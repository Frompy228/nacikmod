package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.registry.*;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.capability.mana.IMana;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;
import java.util.Random;
import java.util.List;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeMod;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.Heightmap;

public class LeonidEntity extends HeroSouls {
    private int shieldBlockCooldown = 0;
    private boolean shieldBlockedHit = false;
    private static final int SHIELD_BLOCK_COOLDOWN = 60; // 3 секунды
    private static final double ATTACK_RANGE = 3.0D;
    private float lastHealth;
    private boolean[] thresholdCrossed = new boolean[3]; // Для отслеживания пересечения порогов 75%, 50%, 25%
    private static final int MAX_MANA = 7100;
    private static final int SPARTAN_SPAWN_MANA_COST = 5000;
    private static final int SPARTAN_SPAWN_RADIUS = 2;
    private int regenerationTick = 0;
    private static final int REGENERATION_INTERVAL = 300;
    private boolean hasSpawnedSpartans = false;
    private static int BONUS_ARMOR = 13;
    
    // Константы для прыжков
    private static final int JUMP_COOLDOWN_TICKS = 60; // 3 секунды между прыжками
    private static final double VERTICAL_JUMP_THRESHOLD = 2.0; // Минимальная разница высоты для прыжка
    private static final double MAX_JUMP_HEIGHT = 4.0; // Максимальная высота прыжка
    
    private int jumpCooldown = 0;

    public LeonidEntity(EntityType<? extends HeroSouls> entityType, Level level) {
        super(entityType, level);
        // Включаем возможность атаки обеими руками
        this.setCanUseBothHands(true);
        // Устанавливаем дальность атаки
        this.setAttackRange(ATTACK_RANGE);
        // Инициализируем lastHealth максимальным здоровьем
        this.lastHealth = this.getMaxHealth();
        // Инициализируем все пороги как непересеченные
        for (int i = 0; i < thresholdCrossed.length; i++) {
            thresholdCrossed[i] = false;
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(ModAttributes.BONUS_ARMOR.get(),BONUS_ARMOR)
                .add(Attributes.ARMOR,20)
                .add(Attributes.ARMOR_TOUGHNESS,10)
                .add(Attributes.MAX_HEALTH, 165.0) // Больше здоровья чем у базового HeroSouls
                .add(Attributes.ATTACK_DAMAGE, 20.0) // Больше урона
                .add(Attributes.MOVEMENT_SPEED, 0.42) // Быстрее базового HeroSouls
                .add(Attributes.FOLLOW_RANGE, 40.0) // Больший радиус обнаружения
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.3)
                .add(ForgeMod.SWIM_SPEED.get(), 2); // Увеличиваем скорость плавания в 1.5 раза
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CustomMeleeAttackGoal(this, 1D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, SpartanEntity.class).setAlertOthers(LeonidEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, this::isValidTarget));
    }

    private boolean isValidTarget(LivingEntity entity) {
        return !(entity instanceof Animal) &&
                !(entity instanceof WaterAnimal) &&
                !(entity instanceof LeonidEntity) &&
                !(entity instanceof SpartanEntity);
    }

    static class CustomMeleeAttackGoal extends Goal {
        private final LeonidEntity leonid;
        private int attackCooldown = 0;
        private final double speedModifier;

        public CustomMeleeAttackGoal(LeonidEntity leonid, double speedModifier) {
            this.leonid = leonid;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = leonid.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = leonid.getTarget();
            if (target == null) return;

            // Двигаемся к цели всегда, если она есть
            leonid.getNavigation().moveTo(target, speedModifier);

            // Поворачиваемся к цели
            leonid.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Проверяем дистанцию и видимость для атаки
            double distanceSqr = leonid.distanceToSqr(target);
            if (distanceSqr <= ATTACK_RANGE * ATTACK_RANGE && leonid.hasLineOfSight(target)) {
                if (attackCooldown <= 0) {
                    leonid.swing(InteractionHand.MAIN_HAND);

                    float totalDamage = (float) leonid.getAttributeValue(Attributes.ATTACK_DAMAGE);

                    // Наносим урон с учетом оружия
                    target.hurt(leonid.damageSources().mobAttack(leonid), totalDamage);

                    attackCooldown = 14;
                } else {
                    attackCooldown--;
                }
            } else {
                // Если не можем атаковать, сбрасываем кулдаун
                attackCooldown = 0;
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Обновляем кулдаун прыжков
        if (jumpCooldown > 0) jumpCooldown--;

        if (shieldBlockCooldown > 0) {
            shieldBlockCooldown--;
            if (shieldBlockCooldown <= 0) {
                shieldBlockedHit = false;
            }
        }

        // Регенерация здоровья
        regenerationTick++;
        if (regenerationTick >= REGENERATION_INTERVAL) {
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(5.0f);
            }
            regenerationTick = 0;
        }

        // Проверяем пересечение порогов здоровья
        float currentHealth = this.getHealth();
        float maxHealth = this.getMaxHealth();
        float healthPercentage = (currentHealth / maxHealth) * 100;

        // Спавним спартанцев при 50% здоровья только на серверной стороне
        if (healthPercentage <= 50 && !hasSpawnedSpartans && !this.level().isClientSide) {
            this.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                if (mana.getMana() >= SPARTAN_SPAWN_MANA_COST) {
                    // Спавним 4 спартанца по углам квадрата
                    spawnSpartans();
                    // Тратим ману
                    mana.removeMana(SPARTAN_SPAWN_MANA_COST);
                    hasSpawnedSpartans = true;
                }
            });
        }

        // Проверяем каждый порог
        if (healthPercentage <= 75 && !thresholdCrossed[0]) {
            applyRoarEffect();
            thresholdCrossed[0] = true;
        }
        if (healthPercentage <= 50 && !thresholdCrossed[1]) {
            applyRoarEffect();
            thresholdCrossed[1] = true;
        }
        if (healthPercentage <= 25 && !thresholdCrossed[2]) {
            applyRoarEffect();
            thresholdCrossed[2] = true;
        }

        // Сбрасываем флаги, если здоровье восстановилось выше порогов
        if (healthPercentage > 75) thresholdCrossed[0] = false;
        if (healthPercentage > 50) {
            thresholdCrossed[1] = false;
            hasSpawnedSpartans = false; // Сбрасываем флаг спавна, чтобы можно было спавнить снова
        }
        if (healthPercentage > 25) thresholdCrossed[2] = false;

        lastHealth = currentHealth;
        
        // Проверяем необходимость прыжка для достижения цели
        LivingEntity target = this.getTarget();
        if (target != null) {
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
            double jumpPower = 0.5 + (this.random.nextDouble() * 0.2);
            this.setDeltaMovement(this.getDeltaMovement().add(0, jumpPower, 0));
            jumpCooldown = JUMP_COOLDOWN_TICKS;
            
            // Проигрываем звук прыжка
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), 
                SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.HOSTILE, 0.5F, 1.2F);
        }
    }

    private void applyRoarEffect() {
        // Получаем все сущности в радиусе 6 блоков
        AABB area = new AABB(
                this.getX() - 6, this.getY() - 6, this.getZ() - 6,
                this.getX() + 6, this.getY() + 6, this.getZ() + 6
        );

        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity target : entities) {
            // Не применяем эффект к самому Леониду и спартанцам
            if (target != this && !(target instanceof SpartanEntity)) {
                target.addEffect(new MobEffectInstance(ModEffects.ROAR.get(), 80, 0, false, false));
            }
        }

        // Проигрываем звук рева
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.ROAR.get(), SoundSource.HOSTILE, 1.0f, 1.0f);
    }

    private void spawnSpartans() {
        if (this.level().isClientSide) return;

        // Получаем позицию Леонида
        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        // Спавним спартанцев по углам квадрата
        spawnSpartanAt(x - SPARTAN_SPAWN_RADIUS, y, z - SPARTAN_SPAWN_RADIUS); // Левый задний угол
        spawnSpartanAt(x + SPARTAN_SPAWN_RADIUS, y, z - SPARTAN_SPAWN_RADIUS); // Правый задний угол
        spawnSpartanAt(x - SPARTAN_SPAWN_RADIUS, y, z + SPARTAN_SPAWN_RADIUS); // Левый передний угол
        spawnSpartanAt(x + SPARTAN_SPAWN_RADIUS, y, z + SPARTAN_SPAWN_RADIUS); // Правый передний угол

        spawnSpartanAt(x - SPARTAN_SPAWN_RADIUS, y, z - SPARTAN_SPAWN_RADIUS); // Левый задний угол
        spawnSpartanAt(x + SPARTAN_SPAWN_RADIUS, y, z - SPARTAN_SPAWN_RADIUS); // Правый задний угол
        spawnSpartanAt(x - SPARTAN_SPAWN_RADIUS, y, z + SPARTAN_SPAWN_RADIUS); // Левый передний угол
        spawnSpartanAt(x + SPARTAN_SPAWN_RADIUS, y, z + SPARTAN_SPAWN_RADIUS); // Правый передний угол
    }

    private void spawnSpartanAt(double x, double y, double z) {
        if (this.level() instanceof ServerLevel serverLevel) {
            SpartanEntity spartan = ModEntities.SPARTAN.get().create(serverLevel);
            if (spartan != null) {
                spartan.setPos(x, y, z);
                spartan.setTarget(this.getTarget()); // Устанавливаем цель спартанца
                // Вызываем finalizeSpawn для инициализации спартанца
                spartan.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spartan.blockPosition()),
                        MobSpawnType.MOB_SUMMONED, null, null);
                serverLevel.addFreshEntity(spartan);
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Проверяем, что урон приходит спереди
        Entity attacker = source.getEntity();
        boolean shieldBlocked = false;
        float originalAmount = amount;
        
        if (attacker != null && this.isUsingItem() && this.getUsedItemHand() == InteractionHand.OFF_HAND &&
                this.getOffhandItem().getItem() == ModItems.LEONID_SHIELD.get() && !shieldBlockedHit) {

            // Проверяем, что атакующий находится спереди
            Vec3 attackerPos = attacker.position();
            Vec3 thisPos = this.position();
            Vec3 lookVec = this.getLookAngle();
            Vec3 toAttacker = attackerPos.subtract(thisPos).normalize();

            // Если атакующий спереди (угол меньше 90 градусов)
            if (lookVec.dot(toAttacker) > 0) {
                // Уменьшаем урон на 80%
                amount *= 0.2f;
                shieldBlocked = true;

                // Отмечаем, что щит заблокировал удар
                shieldBlockedHit = true;
                shieldBlockCooldown = SHIELD_BLOCK_COOLDOWN;
                this.stopUsingItem();

                // Проигрываем звук блокирования
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.SHIELD_BLOCK,
                        this.getSoundSource(), 1.0F, 1.0F);
            }
        }

        boolean isHurt = super.hurt(source, amount);

        // Отражение урона обратно на атакующего при успешном блокировании
        if (shieldBlocked && attacker instanceof LivingEntity livingAttacker && livingAttacker != this) {
            // Вычисляем 70% отраженного урона
            float reflectedDamage = originalAmount * 0.9f;
            
            // Наносим отраженный урон атакующему
            livingAttacker.hurt(this.damageSources().mobAttack(this), reflectedDamage);
            
            // Проигрываем звук отражения урона
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SHIELD_BREAK,
                    this.getSoundSource(), 0.8F, 1.2F);
        }

        if (isHurt) {
            if (attacker instanceof LivingEntity livingAttacker && livingAttacker != this) {
                this.setTarget(livingAttacker);

                // Поднимаем щит при получении урона, если щит не на перезарядке
                if (this.getOffhandItem().getItem() == ModItems.LEONID_SHIELD.get() && shieldBlockCooldown <= 0) {
                    this.startUsingItem(InteractionHand.OFF_HAND);
                }
            }
        }

        return isHurt;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);

        // Выдаём экипировку при спавне
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.LEONID_SHIELD.get()));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.LEONID_HELMET.get()));

        // Инициализируем ману
        this.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            mana.setMaxMana(MAX_MANA);
            mana.setMana(MAX_MANA);
        });

        AttributeInstance attribute = this.getAttribute(ModAttributes.BONUS_ARMOR.get());
        attribute.setBaseValue(BONUS_ARMOR);
        return data;
    }

    @Override
    protected void dropEquipment() {
        // Не дропаем экипировку при смерти
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        Random random = new Random(); // Генератор случайных чисел

        // Шанс дропа в процентах (0.0 - 1.0)
        double chanceLeonidShield = 0.19;
        double chanceLeonidHelmet = 0.11;
        double chanceCircuit = 0.25;

        // Логика дропа с шансом
        if (random.nextDouble() < chanceLeonidShield) {
            this.spawnAtLocation(new ItemStack(ModItems.LEONID_SHIELD.get()));
        }

        if (random.nextDouble() < chanceLeonidHelmet) {
            this.spawnAtLocation(new ItemStack(ModItems.LEONID_HELMET.get()));
        }

        if (random.nextDouble() < chanceCircuit) {
            this.spawnAtLocation(new ItemStack(ModItems.MAGIC_CIRCUIT.get(), 10));
        }
    }

    @Override
    public void dropAllDeathLoot(DamageSource source) {
        this.dropCustomDeathLoot(source, 0, false);
        this.dropExperience(); // Если нужен опыт
    }

    @Override
    protected void dropExperience() {
        if (!this.level().isClientSide) {
            this.level().addFreshEntity(new ExperienceOrb(
                    this.level(),
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    60 // Количество опыта
            ));
        }
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof LeonidEntity || entity instanceof SpartanEntity) {
            return true;
        }
        return super.isAlliedTo(entity);
    }
}