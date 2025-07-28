package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.registry.ModSounds;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.ForgeMod;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.AABB;
import java.util.List;
import java.util.Random;

public class BerserkerEntity extends HeroSouls {
    private int regenerationTick = 0;
    private int roarCooldown = 0;
    private static final int REGENERATION_INTERVAL = 20; // 1 секунда
    private static final float REGENERATION_AMOUNT = 2.0f;
    private static final double BASE_ATTACK_DAMAGE = 22;
    private static final double ENTITY_REACH = 5.0D; // Достигаемость сущностей 5 блоков
    private static final int ROAR_COOLDOWN = 400; // 15 секунд (20 тиков * 15)
    private static final int ROAR_RADIUS = 2; // Радиус разрушения блоков
    private static final int MAX_RESURRECTIONS = 7; // Максимальное количество воскрешений
    private int resurrectionCount = 0; // Счетчик воскрешений
    private static final int BASE_ATTACK_COOLDOWN = 50; // 2 секунды между атаками
    private int currentAttackCooldown = BASE_ATTACK_COOLDOWN; // Текущая перезарядка атаки

    // Флаг для защиты от повторного входа в performRoar
    private boolean isRoaring = false;

    public BerserkerEntity(EntityType<? extends HeroSouls> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new CustomMeleeAttackGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, this::isValidTarget));
    }

    private boolean isValidTarget(LivingEntity entity) {
        return !(entity instanceof Animal) &&
                !(entity instanceof WaterAnimal) &&
                !(entity instanceof BerserkerEntity);
    }

    static class CustomMeleeAttackGoal extends MeleeAttackGoal {
        private final BerserkerEntity berserker;
        private static final double AREA_DAMAGE_RADIUS = 2; // Радиус урона по области
        private int attackCooldown = 0;

        public CustomMeleeAttackGoal(BerserkerEntity mob, double speedModifier) {
            super(mob, speedModifier, true);
            this.berserker = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public void tick() {
            LivingEntity target = berserker.getTarget();
            if (target == null) return;

            double distanceSqr = berserker.distanceToSqr(target);
            if (distanceSqr <= ENTITY_REACH * ENTITY_REACH && berserker.hasLineOfSight(target)) {
                if (attackCooldown <= 0) {
                    berserker.swing(InteractionHand.MAIN_HAND);

                    // Наносим урон основной цели
                    berserker.doHurtTarget(target);

                    // Наносим урон всем сущностям в радиусе
                    AABB area = new AABB(
                            target.getX() - AREA_DAMAGE_RADIUS, target.getY() - AREA_DAMAGE_RADIUS, target.getZ() - AREA_DAMAGE_RADIUS,
                            target.getX() + AREA_DAMAGE_RADIUS, target.getY() + AREA_DAMAGE_RADIUS, target.getZ() + AREA_DAMAGE_RADIUS
                    );

                    List<LivingEntity> nearbyEntities = berserker.level().getEntitiesOfClass(
                            LivingEntity.class,
                            area,
                            entity -> entity != target && entity != berserker && berserker.canAttack(entity)
                    );

                    for (LivingEntity entity : nearbyEntities) {
                        berserker.doHurtTarget(entity);
                    }

                    attackCooldown = berserker.currentAttackCooldown;
                } else {
                    attackCooldown--;
                }
            }

            super.tick();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(ModAttributes.BONUS_ARMOR.get(), 10)
                .add(Attributes.ARMOR, 17)
                .add(Attributes.ARMOR_TOUGHNESS, 15)
                .add(Attributes.MAX_HEALTH, 115.0)
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(ForgeMod.SWIM_SPEED.get(), 2)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(ForgeMod.ENTITY_REACH.get(), ENTITY_REACH);
    }

    @Override
    public void tick() {
        super.tick();

        // Регенерация здоровья
        regenerationTick++;
        if (regenerationTick >= REGENERATION_INTERVAL) {
            if (this.getHealth() < this.getMaxHealth()) {
                this.heal(REGENERATION_AMOUNT);
            }
            regenerationTick = 0;
        }

        // Обработка перезарядки рычания
        if (roarCooldown > 0) {
            roarCooldown--;
        }
    }

    private void performRoar() {
        // Проигрываем звук крика
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                ModSounds.BERSERKER_ROAR.get(), SoundSource.HOSTILE, 1.0f, 1.0f);

        // Наносим урон всем сущностям в радиусе
        AABB area = new AABB(
                this.getX() - ROAR_RADIUS, this.getY() - ROAR_RADIUS, this.getZ() - ROAR_RADIUS,
                this.getX() + ROAR_RADIUS, this.getY() + ROAR_RADIUS, this.getZ() + ROAR_RADIUS
        );
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, area);
        for (LivingEntity target : entities) {
            if (target != this) { // Не наносим урон самому себе
                target.hurt(this.damageSources().mobAttack(this), 7.0f);
            }
        }

        // Разрушаем блоки в радиусе, исключая блоки под ногами
        BlockPos centerPos = this.blockPosition();
        for (int x = -ROAR_RADIUS; x <= ROAR_RADIUS; x++) {
            for (int y = -ROAR_RADIUS; y <= ROAR_RADIUS; y++) {
                for (int z = -ROAR_RADIUS; z <= ROAR_RADIUS; z++) {
                    // Пропускаем блоки под ногами (y < 0)
                    if (y < 0) continue;

                    BlockPos pos = centerPos.offset(x, y, z);
                    BlockState state = this.level().getBlockState(pos);

                    // Проверяем, что блок не воздух и имеет скорость разрушения
                    if (!state.isAir() && state.getDestroySpeed(this.level(), pos) > 0) {
                        this.level().destroyBlock(pos, true);
                    }
                }
            }
        }

        // Добавляем эффект отскока при крике
        this.setDeltaMovement(this.getDeltaMovement().add(0, 0.3, 0));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Не ревём и не обрабатываем урон, если уже мертвы
        if (!this.isAlive()) return super.hurt(source, amount);
        // Проверяем, что урон приходит от сущности и не от самого себя
        if (!this.level().isClientSide && source.getEntity() != this && roarCooldown <= 0 && !isRoaring) {
            isRoaring = true;
            performRoar();
            isRoaring = false;
            roarCooldown = ROAR_COOLDOWN;
        }

        if (!this.level().isClientSide && this.getHealth() <= amount && resurrectionCount < MAX_RESURRECTIONS) {
            // Увеличиваем счетчик воскрешений
            resurrectionCount++;

            // Восстанавливаем здоровье
            this.setHealth(this.getMaxHealth());

            // Увеличиваем скорость
            AttributeInstance speedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.setBaseValue(speedAttribute.getBaseValue() * 1.1);
            }

            // Увеличиваем скорость атаки (уменьшаем перезарядку)
            currentAttackCooldown = (int)(currentAttackCooldown * 0.9); // Уменьшаем на 10%

            // Увеличиваем урон
            AttributeInstance attackDamageAttribute = this.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attackDamageAttribute != null) {
                attackDamageAttribute.setBaseValue(BASE_ATTACK_DAMAGE * 1.1);
            }

            // Добавляем эффект отскока
            this.setDeltaMovement(0, 0.5, 0);

            // Вызываем молнию
            LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(this.level());
            if (lightning != null) {
                lightning.setPos(this.getX(), this.getY(), this.getZ());
                this.level().addFreshEntity(lightning);
            }

            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.DUBINKA.get()));

        AttributeInstance attribute = this.getAttribute(ModAttributes.BONUS_ARMOR.get());
        attribute.setBaseValue(10.0);
        return data;
    }
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        Random random = new Random(); // Генератор случайных чисел
        double chanceCircuit = 0.25;
        double chanceGodHand = 0.19;

        if (random.nextDouble() < chanceCircuit) {
            this.spawnAtLocation(new ItemStack(ModItems.MAGIC_CIRCUIT.get(), 20));
        }
        if (random.nextDouble() < chanceGodHand) {
            this.spawnAtLocation(new ItemStack(ModItems.GOD_HAND.get(), 1));
        }
    }


    @Override
    protected void dropEquipment() {
        // Пусто - не дропаем экипировку из рук
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        // Пусто - не генерируем случайную экипировку
    }

    // Если нужно сохранить опыт, но убрать стандартный дроп
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
                    300 // Количество опыта
            ));
        }
    }
}
