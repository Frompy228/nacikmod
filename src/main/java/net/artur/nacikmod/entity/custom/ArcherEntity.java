package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.entity.ai.BreakBlockGoal;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;
import net.artur.nacikmod.item.Intangibility;
import net.artur.nacikmod.capability.mana.IMana;

public class ArcherEntity extends HeroSouls implements RangedAttackMob {
    private static final int MAX_MANA = 12500;
    private static final double OPTIMAL_DISTANCE = 20.0;
    private static final double SWITCH_TO_MELEE_DISTANCE = 6;
    private static final double SWITCH_TO_RANGED_DISTANCE = 7.0;
    private static final double MAX_SHOOT_DISTANCE = 32.0;
    private static final double SHOOT_SPEED = 2.8;
    private static final int SHOOT_COOLDOWN_TICKS = 19;
    private static final int MELEE_COOLDOWN_TICKS = 11;
    private static final int ROOT_ABILITY_COOLDOWN_TICKS = 300; // 20 секунд
    private static final int ROOT_ABILITY_MANA_COST = 3000;
    private static final int ROOT_ABILITY_DURATION = 180; // 7 секунд
    private static int BONUS_ARMOR = 17;
    
    // Константы для прыжков
    private static final int JUMP_COOLDOWN_TICKS = 60; // 3 секунды между прыжками
    private static final double VERTICAL_JUMP_THRESHOLD = 2.0; // Минимальная разница высоты для прыжка
    
    private int shootCooldown = 0;
    private int rootAbilityCooldown = 0;
    private int jumpCooldown = 0;
    private boolean attackWithMainHand = true;

    public ArcherEntity(EntityType<? extends HeroSouls> entityType, Level level) {
        super(entityType, level);
        this.setCanUseBothHands(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(ModAttributes.BONUS_ARMOR.get(), BONUS_ARMOR)
                .add(Attributes.ARMOR, 15)
                .add(Attributes.ARMOR_TOUGHNESS, 10)
                .add(Attributes.MAX_HEALTH, 185.0)
                .add(Attributes.ATTACK_DAMAGE, 21.0)
                .add(Attributes.MOVEMENT_SPEED, 0.44)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                .add(ForgeMod.SWIM_SPEED.get(), 2)
                .add(Attributes.FOLLOW_RANGE, 50.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreakBlockGoal(this));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true)); // Открытие дверей во время боя
        this.goalSelector.addGoal(3, new ArcherRangedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new ArcherMeleeGoal(this, 1.0));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Mob.class, 40, true, false, this::isValidTarget));
    }

    private boolean isValidTarget(LivingEntity entity) {
        return !(entity instanceof Animal) && !(entity instanceof WaterAnimal);
    }

    @Override
    public void tick() {
        super.tick();
        
        // Обновляем кулдауны
        if (shootCooldown > 0) shootCooldown--;
        if (jumpCooldown > 0) jumpCooldown--;
        
        // Root-способность только если есть цель
        LivingEntity target = this.getTarget();
        if (target != null) {
            if (rootAbilityCooldown > 0) rootAbilityCooldown--;
            if (!this.level().isClientSide && rootAbilityCooldown == 0) {
                // Проверяем блокировку способностей
                if (!isActionBlocked(ActionType.ABILITY_CAST)) {
                    this.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                        if (mana.getMana() >= ROOT_ABILITY_MANA_COST && this.isValidTarget(target) && target.isAlive() && this.hasLineOfSight(target)) {
                            target.addEffect(new MobEffectInstance(ModEffects.ROOT.get(), ROOT_ABILITY_DURATION, 0, false, true));
                            mana.removeMana(ROOT_ABILITY_MANA_COST);
                            rootAbilityCooldown = ROOT_ABILITY_COOLDOWN_TICKS;
                            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.PLAYERS, 1.0F, 1.0F);
                        }
                    });
                }
            }
            
            // Проверяем необходимость прыжка для достижения цели
            checkAndPerformJump(target);
        }
    }
    
    /**
     * Проверяет необходимость прыжка и выполняет его
     */
    /**
     * Проверяет необходимость прыжка и выполняет его
     */
    private void checkAndPerformJump(LivingEntity target) {
        if (jumpCooldown > 0 || !this.onGround()) return;

        double targetY = target.getY();
        double thisY = this.getY();
        double heightDifference = targetY - thisY;

        // Проверяем горизонтальную дистанцию до цели
        double horizontalDistance = this.distanceTo(target);

        // Если цель выше и разница значительная, и находится на близкой дистанции (до 5 блоков), пытаемся прыгнуть
        if (heightDifference > VERTICAL_JUMP_THRESHOLD &&
                horizontalDistance <= 5.0) {
            // Проверяем, есть ли препятствия между нами и целью
            if (this.hasLineOfSight(target)) {
                performJump();
            }
        }
    }
    
    /**
     * Выполняет прыжок вверх
     */
    private void performJump() {
        if (this.onGround() && jumpCooldown <= 0) {
            // Прыгаем вверх с небольшой случайностью в направлении
            double jumpPower = 0.6 + (this.random.nextDouble() * 0.2);
            this.setDeltaMovement(this.getDeltaMovement().add(0, jumpPower, 0));
            jumpCooldown = JUMP_COOLDOWN_TICKS;

        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean isHurt = super.hurt(source, amount);
        if (isHurt) {
            Entity attacker = source.getEntity();
            if (attacker instanceof LivingEntity livingAttacker && livingAttacker != this) {
                this.setTarget(livingAttacker);
            }
        }
        return isHurt;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (isActionBlocked(ActionType.RANGED_ATTACK)) return;

        if (!this.level().isClientSide) {
            this.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, target.position());

            net.artur.nacikmod.entity.projectiles.ManaArrowProjectile arrow =
                    new net.artur.nacikmod.entity.projectiles.ManaArrowProjectile(this.level(), this);

            // Арчер всегда стреляет "идеально", поэтому power = 1.0F
            float pullPower = 1.0F;

            // 1. Устанавливаем высокую скорость (как у игрока на максе или чуть выше)
            float velocity = (float)SHOOT_SPEED * 1.8F; // 2.8 * 1.8 = ~5.0 (как у игрока)

            // 2. Рассчитываем базовый урон на основе ATTACK_DAMAGE моба (сейчас это 21.0)
            // Делим на 2, так как скорость 5.0 сильно множит результат
            double baseDamage = this.getAttributeValue(Attributes.ATTACK_DAMAGE) / 2.5;

            arrow.shoot(target.getX() - this.getX(),
                    target.getY(0.333) - arrow.getY() + (Math.sqrt(Math.pow(target.getX() - this.getX(), 2) + Math.pow(target.getZ() - this.getZ(), 2)) * 0.05F),
                    target.getZ() - this.getZ(),
                    velocity, 0.0F);

            // Устанавливаем урон снаряду
            arrow.setBaseDamage(baseDamage);

            this.level().addFreshEntity(arrow);

            // Звук выстрела
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.ARROW_SHOOT, SoundSource.HOSTILE, 1.0F, 0.8F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.MAGIC_BOW.get()));
        this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        AttributeInstance attribute = this.getAttribute(ModAttributes.BONUS_ARMOR.get());
        attribute.setBaseValue(BONUS_ARMOR);
        // Устанавливаем максимальную ману 10000
        this.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            mana.setMaxMana(MAX_MANA);
            mana.setMana(MAX_MANA);
        });
        return data;
    }

    // --- AI Goals ---
    static class ArcherRangedGoal extends Goal {
        private final ArcherEntity archer;
        private final double speedModifier;
        public ArcherRangedGoal(ArcherEntity archer, double speedModifier) {
            this.archer = archer;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }
        @Override
        public boolean canUse() {
            LivingEntity target = archer.getTarget();
            return target != null && archer.distanceTo(target) > SWITCH_TO_MELEE_DISTANCE;
        }
        @Override
        public boolean canContinueToUse() {
            LivingEntity target = archer.getTarget();
            return target != null && archer.distanceTo(target) > SWITCH_TO_MELEE_DISTANCE;
        }
        @Override
        public void start() {
            archer.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.MAGIC_BOW.get()));
            archer.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        @Override
        public void tick() {
            LivingEntity target = archer.getTarget();
            if (target == null) return;
            // Защита от самоатаки
            if (target == archer) {
                archer.setTarget(null);
                return;
            }
            double dist = archer.distanceTo(target);
            // Движение: держим дистанцию
            if (dist > OPTIMAL_DISTANCE + 2) {
                archer.getNavigation().moveTo(target, speedModifier);
            } else if (dist < OPTIMAL_DISTANCE - 2) {
                Vec3 away = archer.position().subtract(target.position()).normalize();
                archer.getNavigation().moveTo(archer.getX() + away.x * 2, archer.getY(), archer.getZ() + away.z * 2, speedModifier);
            } else {
                // Круговое движение
                double angle = (archer.tickCount % 40 < 20) ? Math.PI / 2 : -Math.PI / 2;
                Vec3 dir = target.position().subtract(archer.position()).normalize();
                double x = -dir.z * Math.sin(angle);
                double z = dir.x * Math.sin(angle);
                double randomOffset = (archer.getRandom().nextDouble() - 0.5) * 2.0;
                archer.getNavigation().moveTo(
                    archer.getX() + x + randomOffset,
                    archer.getY(),
                    archer.getZ() + z + randomOffset,
                    speedModifier
                );
            }
            // Стрельба
            if (archer.shootCooldown == 0 && archer.hasLineOfSight(target) && dist <= MAX_SHOOT_DISTANCE) {
                archer.performRangedAttack(target, 1.0F);
                archer.shootCooldown = SHOOT_COOLDOWN_TICKS;
            }
        }
    }

    static class ArcherMeleeGoal extends Goal {
        private final ArcherEntity archer;
        private final double speedModifier;
        private int attackCooldown = 0;

        public ArcherMeleeGoal(ArcherEntity archer, double speedModifier) {
            this.archer = archer;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }
        @Override
        public boolean canUse() {
            LivingEntity target = archer.getTarget();
            return target != null && archer.distanceTo(target) <= SWITCH_TO_MELEE_DISTANCE;
        }
        @Override
        public boolean canContinueToUse() {
            LivingEntity target = archer.getTarget();
            return target != null && archer.distanceTo(target) <= SWITCH_TO_RANGED_DISTANCE;
        }
        @Override
        public void start() {
            archer.setItemSlot(EquipmentSlot.MAINHAND, ModItems.CURSED_SWORD.get().getDefaultInstance());
            archer.setItemSlot(EquipmentSlot.OFFHAND, ModItems.CURSED_SWORD.get().getDefaultInstance());
            attackCooldown = 0;
        }
        @Override
        public void tick() {
            LivingEntity target = archer.getTarget();
            if (target == null) return;
            // Защита от самоатаки
            if (target == archer) {
                archer.setTarget(null);
                return;
            }
            archer.getNavigation().moveTo(target, speedModifier);
            archer.getLookControl().setLookAt(target, 30.0F, 30.0F);
            // Гарантируем, что в обеих руках кастомные мечи
            if (!(archer.getMainHandItem().getItem() == ModItems.CURSED_SWORD.get() && archer.getOffhandItem().getItem() == ModItems.CURSED_SWORD.get())) {
                archer.setItemSlot(EquipmentSlot.MAINHAND, ModItems.CURSED_SWORD.get().getDefaultInstance());
                archer.setItemSlot(EquipmentSlot.OFFHAND, ModItems.CURSED_SWORD.get().getDefaultInstance());

            }
            double dist = archer.distanceTo(target);
            if (attackCooldown > 0) { attackCooldown--; return; }
            if (dist < 3.5 && archer.hasLineOfSight(target)) {
                // --- Intangibility bypass logic ---
                if (target instanceof Player player) {
                    // Атакуем
                    InteractionHand hand = archer.attackWithMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                    archer.swing(hand);
                    archer.doHurtTarget(target);
                    archer.attackWithMainHand = !archer.attackWithMainHand;
                    attackCooldown = MELEE_COOLDOWN_TICKS;

                    return;
                }
                // --- Обычные цели ---
                InteractionHand hand = archer.attackWithMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                archer.swing(hand);
                archer.doHurtTarget(target);
                archer.attackWithMainHand = !archer.attackWithMainHand;
                attackCooldown = MELEE_COOLDOWN_TICKS;
                // Сжигаем ману у цели (не уходит в минус)
                target.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                    int currentMana = mana.getMana();
                    mana.removeMana(Math.min(currentMana, 50));
                });
                // --- Дополнительный урон за бонусную броню ---
                AttributeInstance bonusArmor = target.getAttribute(ModAttributes.BONUS_ARMOR.get());
                double bonus = bonusArmor != null ? bonusArmor.getValue() : 0.0;
                float extraDamage = (float) (bonus * 0.45);
                if (extraDamage > 0) {
                    target.hurt(archer.damageSources().mobAttack(archer), extraDamage);
                }
            }
        }
    }
    
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        Random random = new Random();
        double chanceMagicBow = 0.10;
        double chanceCursedSword = 0.18;
        double chanceCircuit = 0.27;

        if (random.nextDouble() < chanceMagicBow) {
            this.spawnAtLocation(new ItemStack(ModItems.MAGIC_BOW.get()));
        }
        if (random.nextDouble() < chanceCursedSword) {
            this.spawnAtLocation(new ItemStack(ModItems.CURSED_SWORD.get()));
        }
        if (random.nextDouble() < chanceCircuit) {
            int circuitCount = random.nextInt(13, 17);
            this.spawnAtLocation(new ItemStack(ModItems.MAGIC_CIRCUIT.get(), circuitCount));
        }
    }

    @Override
    protected void dropEquipment() {
        // Не дропаем экипировку из рук
    }

    @Override
    public void dropAllDeathLoot(DamageSource source) {
        this.dropCustomDeathLoot(source, 0, false);
        this.dropExperience();
    }

    @Override
    protected void dropExperience() {
        if (!this.level().isClientSide) {
            this.level().addFreshEntity(new net.minecraft.world.entity.ExperienceOrb(
                    this.level(),
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    80 // Количество опыта
            ));
        }
    }
}

