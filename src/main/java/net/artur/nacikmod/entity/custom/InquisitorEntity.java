
package net.artur.nacikmod.entity.custom;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.entity.ai.BreakBlockGoal;
import net.artur.nacikmod.entity.ai.InquisitorGuardGoal;
import net.artur.nacikmod.entity.projectiles.FireCloudEntity;
import net.artur.nacikmod.entity.projectiles.FireHailEntity;
import net.artur.nacikmod.entity.projectiles.FireWallEntity;
import net.artur.nacikmod.entity.projectiles.FireBallEntity;
import net.artur.nacikmod.entity.projectiles.FireAnnihilationEntity;
import net.artur.nacikmod.item.Gravity;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InquisitorEntity extends HeroSouls {
    private static final EntityDataAccessor<Integer> DATA_MANA = SynchedEntityData.defineId(InquisitorEntity.class, EntityDataSerializers.INT);
    private static final int MAX_MANA = 30_000;
    private static final int MANA_REGEN_PER_TICK = 30;
    private static final double ATTACK_RANGE = 3.0D;
    private static final int ATTACK_COOLDOWN_TICKS = 10;
    private static final int GLOBAL_COOLDOWN_TICKS = 95;
    private static int BONUS_ARMOR = 30;

    private static final int SUMMON_CAST_TICKS = 60;
    private static final int SUMMON_COOLDOWN_TICKS = 4700; // 5 minutes
    private static final int SUMMON_MANA_COST = 10_000;
    private static final float SUMMON_INTERRUPT_DAMAGE = 30.0F;

    private static final int FIRE_WALL_COOLDOWN_TICKS = 400; // 20 seconds
    private static final int FIRE_WALL_MANA_COST = 500;

    private static final int FIRE_HAIL_COOLDOWN_TICKS = 280; // 20 seconds
    private static final int FIRE_HAIL_MANA_COST = 500;
    private static final double FIRE_HAIL_RADIUS = 2.0D; // Радиус круга
    private static final int FIRE_HAIL_COUNT = 6; // Количество снарядов в круге

    private static final int FIREBALL_JUMP_COOLDOWN_TICKS = 280; // 20 seconds
    private static final int FIREBALL_JUMP_MANA_COST = 600;
    private static final double BACKWARD_STRENGTH = 0.9; // сила отскока назад
    private static final double UPWARD_STRENGTH = 0.5;   // сила прыжка вверх
    private static final int JUMP_COOLDOWN_TICKS = 40;   // задержка между прыжками

    // Константы для обычного прыжка вверх
    private static final int VERTICAL_JUMP_COOLDOWN_TICKS = 60; // 3 секунды между прыжками
    private static final double VERTICAL_JUMP_THRESHOLD = 2.0; // Минимальная разница высоты для прыжка
    private static final double VERTICAL_JUMP_MIN_POWER = 0.7; // Минимальная сила прыжка (выше чем у Leonid)
    private static final double VERTICAL_JUMP_MAX_POWER = 0.9; // Максимальная сила прыжка
    private static final double VERTICAL_JUMP_RANGE = 5.0; // Максимальная горизонтальная дистанция для прыжка

    private static final int BLESSING_COOLDOWN_TICKS = 1200; // 60 секунд
    private static final int BLESSING_MANA_COST = 700;
    private static final int BLESSING_DURATION_TICKS = 600; // 30 секунд

    private static final int FIRE_ANNIHILATION_COOLDOWN_TICKS = 300; // 15 секунд
    private static final int FIRE_ANNIHILATION_MANA_COST = 750;
    private static final int FIRE_ANNIHILATION_COUNT = 3; // Количество выстрелов
    private static final int FIRE_ANNIHILATION_INTERVAL_TICKS = 5; // Интервал между выстрелами (1 секунда)
    private static final double FIRE_ANNIHILATION_SPAWN_DISTANCE = 2.5D; // Дистанция спавна от инквизитора



    // Отдельные способности (вне основного списка)
    private static final int ANTI_FLIGHT_COOLDOWN_TICKS = 200; // 10 секунд
    private static final int ANTI_FLIGHT_MANA_COST = 300;
    private static final int ANTI_FLIGHT_LIGHTNING_COUNT = 5;
    private static final double ANTI_FLIGHT_LIGHTNING_SPREAD = 0.6D;
    public static final String ANTI_FLIGHT_LIGHTNING_TAG = "nacikmod_inquisitor_anti_flight";

    private static final int DASH_COOLDOWN_TICKS = 500; // 25 seconds
    private static final float DASH_DAMAGE = 20.0F;
    private static final double DASH_DISTANCE = 8.0D; // Максимальная дистанция для рывка

    private static final int TELEPORT_COOLDOWN_TICKS = 360;
    private static final int TELEPORT_MANA_COST = 300;
    private static final int TELEPORT_MANA_BURN = 300; // Сжигание маны у цели

    private static final int FIRE_CLOUD_COOLDOWN_TICKS = 260;
    private static final int FIRE_CLOUD_MANA_COST = 200;
    private static final double FIRE_CLOUD_RADIUS = 10.0D; // Радиус вокруг инквизитора
    private static final int FIRE_CLOUD_COUNT = 3; // Количество облаков

    private enum AbilityType {
        NONE(0, 0, 0),
        SUMMON_HERO_SOUL( SUMMON_MANA_COST, SUMMON_COOLDOWN_TICKS, SUMMON_CAST_TICKS),
        FIRE_WALL(FIRE_WALL_MANA_COST, FIRE_WALL_COOLDOWN_TICKS, 0), // Моментальная, без каста
        FIRE_HAIL(FIRE_HAIL_MANA_COST, FIRE_HAIL_COOLDOWN_TICKS, 0), // Моментальная, без каста
        FIREBALL_JUMP(FIREBALL_JUMP_MANA_COST, FIREBALL_JUMP_COOLDOWN_TICKS, 0), // Моментальная, без каста
        HOLY_BLESSING(BLESSING_MANA_COST, BLESSING_COOLDOWN_TICKS, 0), // Моментальная, без каста
        FIRE_ANNIHILATION(FIRE_ANNIHILATION_MANA_COST, FIRE_ANNIHILATION_COOLDOWN_TICKS, 0); // Моментальная, без каста

        private final int manaCost;
        private final int cooldownTicks;
        private final int castTicks;

        AbilityType(int manaCost, int cooldownTicks, int castTicks) {
            this.manaCost = manaCost;
            this.cooldownTicks = cooldownTicks;
            this.castTicks = castTicks;
        }
    }

    private AbilityType activeAbility = AbilityType.NONE;
    private int castTicks;
    private float damageTakenThisCast;
    private int nextAbilityRollTick = GLOBAL_COOLDOWN_TICKS;
    private int fireballJumpTicks = 0; // Счетчик тиков для задержки выстрела после прыжка
    private LivingEntity fireballJumpTarget = null; // Цель для выстрела после прыжка
    private int jumpCooldown = 0; // Кулдаун прыжка для Fireball Jump
    private int verticalJumpCooldown = 0; // Кулдаун для обычного вертикального прыжка
    private int fireAnnihilationTicks = 0; // Счетчик тиков для интервала между выстрелами FireAnnihilation
    private int fireAnnihilationCount = 0; // Счетчик оставшихся выстрелов FireAnnihilation
    private LivingEntity fireAnnihilationTarget = null; // Цель для FireAnnihilation
    private final Object2IntMap<AbilityType> abilityCooldowns = new Object2IntOpenHashMap<>();
    private Vec3 pendingSummonPosition;
    private AreaEffectCloud telegraphCloud;

    // Кулдауны для отдельных способностей
    private int dashCooldown = 0;
    private int teleportCooldown = 0;
    private int antiFlightCooldown = 0;
    private int fireCloudCooldown = 0;

    // Отслеживание рывка для нанесения урона при столкновении
    private boolean isDashing = false;
    private LivingEntity dashTarget = null;

    // Отслеживание здоровья для переключения на Holy_bayonets
    private float initialHealth = 100.0F;
    private static final float HP_THRESHOLD_FOR_WEAPON_SWITCH = 50.0F; // Переключаемся после потери 50 HP
    private boolean hasSwitchedToHolyBayonets = false;

    private static final List<HeroSoulOption> HERO_SOUL_POOL = buildHeroSoulPool();
    private static final int HERO_SOUL_TOTAL_WEIGHT = HERO_SOUL_POOL.stream().mapToInt(HeroSoulOption::weight).sum();

    public InquisitorEntity(EntityType<? extends HeroSouls> entityType, Level level) {
        super(entityType, level);
        this.setPersistenceRequired();
        this.setCanUseBothHands(true);
        this.setAttackRange(ATTACK_RANGE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(ModAttributes.BONUS_ARMOR.get(), BONUS_ARMOR)
                .add(Attributes.MAX_HEALTH, 185.0D)
                .add(Attributes.ARMOR, 14.0D)
                .add(Attributes.ARMOR_TOUGHNESS, 5.0D)
                .add(Attributes.ATTACK_DAMAGE, 24.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.38D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.2D)
                .add(ForgeMod.SWIM_SPEED.get(), 2)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_MANA, MAX_MANA);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreakBlockGoal(this));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true)); // Открытие дверей во время боя
        this.goalSelector.addGoal(3, new InquisitorMeleeAttackGoal(this, 1.2D));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        // Убрали автоматическую атаку на игроков - инквизитор нейтрален до получения урона
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, this::isValidTarget));
    }

    private boolean isValidTarget(@Nullable net.minecraft.world.entity.LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        // Не атакуем других инквизиторов
        if (entity instanceof InquisitorEntity) {
            return false;
        }

        // Не атакуем своих призванных сущностей
        if (entity.getPersistentData().contains("inquisitor_owner")) {
            try {
                UUID summonedOwnerUUID = entity.getPersistentData().getUUID("inquisitor_owner");
                if (summonedOwnerUUID != null && summonedOwnerUUID.equals(this.getUUID())) {
                    return false;
                }
            } catch (Exception ignored) {
            }
        }

        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            handleManaRegeneration();
            maintainRegenerationEffect();
            tickAbilitySystem();
            tickSeparateAbilities(); // Отдельные способности (рывок и телепорт)
            checkDashCollision(); // Проверка столкновения во время рывка
            tickFireballJump(); // Обработка задержки выстрела после прыжка
            tickFireAnnihilation(); // Обработка выстрелов FireAnnihilation с интервалом
            checkAndSwitchWeapon(); // Проверка необходимости переключения на Holy_bayonets
            
            // Обновляем кулдауны прыжков
            if (jumpCooldown > 0) {
                jumpCooldown--;
            }
            if (verticalJumpCooldown > 0) {
                verticalJumpCooldown--;
            }
            
            // Проверяем необходимость обычного прыжка вверх для достижения цели
            if (!isCastingAbility() && !isDashing) {
                LivingEntity target = this.getTarget();
                if (target != null) {
                    checkAndPerformVerticalJump(target);
                }
            }
        } else if (this.isCastingAbility()) {
            this.getNavigation().stop();
        }
    }

    private void handleManaRegeneration() {
        if (this.tickCount % 5 == 0 && this.getMana() < MAX_MANA) {
            addMana(MANA_REGEN_PER_TICK);
        }
    }

    private void maintainRegenerationEffect() {
        if (this.tickCount % 60 == 0) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 3, false, false));
        }
    }

    private void tickAbilitySystem() {
        if (isCastingAbility()) {
            this.getNavigation().stop();
            this.setDeltaMovement(Vec3.ZERO);
            checkInterruptsDuringCast();
            tickActiveAbility();
            return;
        }

        if (this.tickCount >= nextAbilityRollTick) {
            tryStartRandomAbility();
        }
    }

    /**
     * Обрабатывает отдельные способности (рывок и телепорт), которые не входят в основной список
     */
    private void tickSeparateAbilities() {
        // Обновляем кулдауны
        if (dashCooldown > 0) dashCooldown--;
        if (teleportCooldown > 0) teleportCooldown--;
        if (antiFlightCooldown > 0) antiFlightCooldown--;
        if (fireCloudCooldown > 0) fireCloudCooldown--;

        // Используем способности только в бою и если не кастуем основную способность
        if (isCastingAbility()) {
            return;
        }
        
        // Проверяем блокировку способностей под эффектом SUPPRESSING_GATE
        if (isActionBlocked(HeroSouls.ActionType.ABILITY_CAST)) {
            return;
        }

        // Нельзя использовать рывок и телепорт одновременно
        if (isDashing) {
            return; // Если в процессе рывка, не используем телепорт
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }

        // Проверяем анти-полет способность (приоритет над остальными)
        if (antiFlightCooldown <= 0 && canUseAntiFlightAbility(target)) {
            if (performAntiFlightLightning(target)) {
                antiFlightCooldown = ANTI_FLIGHT_COOLDOWN_TICKS;
                return;
            }
        }

        // Проверяем возможность использования облаков огня
        if (fireCloudCooldown <= 0 && this.getMana() >= FIRE_CLOUD_MANA_COST) {
            performFireCloudAbility();
            fireCloudCooldown = FIRE_CLOUD_COOLDOWN_TICKS;
            return; // После активации не проверяем другие способности в этом тике
        }

        // Проверяем возможность использования рывка (приоритет над телепортом)
        if (dashCooldown <= 0 && !isDashing) {
            double distanceToTarget = this.distanceTo(target);
            // Рывок используется на короткой дистанции (от 3 до 8 блоков)
            // Не используем dash если ближе 3 блоков к цели
            if (distanceToTarget <= DASH_DISTANCE && distanceToTarget >= 3.0D && this.hasLineOfSight(target)) {
                performDash(target);
                dashCooldown = DASH_COOLDOWN_TICKS;
                return; // После активации рывка не проверяем телепорт
            }
        }

        // Проверяем возможность использования телепорта (только если не в рывке)
        if (!isDashing && teleportCooldown <= 0 && this.getMana() >= TELEPORT_MANA_COST) {
            // Телепорт можно использовать на любой дистанции
            performTeleport(target);
            teleportCooldown = TELEPORT_COOLDOWN_TICKS;
        }
    }

    /**
     * Выполняет скоростной рывок к цели (урон наносится при столкновении)
     */
    private void performDash(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }

        // Вычисляем направление рывка
        Vec3 dashDirection = target.position().subtract(this.position()).normalize();
        // Рывок на короткую дистанцию, но быстрый
        double dashPower = 2.2D; // Сила рывка
        Vec3 dashVelocity = dashDirection.scale(dashPower);

        // Применяем рывок
        this.setDeltaMovement(dashVelocity.x, 0.3, dashVelocity.z);
        this.hurtMarked = true; // Обновляем движение

        // Устанавливаем флаг рывка и сохраняем цель для проверки столкновения
        this.isDashing = true;
        this.dashTarget = target;
    }

    /**
     * Проверяет столкновение с целью во время рывка и наносит урон при достижении
     */
    private void checkDashCollision() {
        if (!isDashing || dashTarget == null) {
            return;
        }

        // Если цель мертва или не существует, прекращаем рывок
        if (!dashTarget.isAlive()) {
            isDashing = false;
            dashTarget = null;
            return;
        }

        // Проверяем дистанцию до цели
        double distanceToTarget = this.distanceTo(dashTarget);

        // Если достигли цели (в пределах дистанции атаки + небольшой запас)
        if (distanceToTarget <= ATTACK_RANGE + 0.5D) {
            // Наносим урон при столкновении
            dashTarget.hurt(this.damageSources().mobAttack(this), DASH_DAMAGE);
            this.swing(InteractionHand.MAIN_HAND);
            this.swing(InteractionHand.OFF_HAND);

            // Прекращаем рывок
            isDashing = false;
            dashTarget = null;
        } else if (distanceToTarget > DASH_DISTANCE * 1.5D) {
            // Если ушли слишком далеко от цели, прекращаем рывок (промах)
            isDashing = false;
            dashTarget = null;
        } else if (this.onGround() && this.getDeltaMovement().horizontalDistance() < 0.1D) {
            // Если остановились на земле (столкнулись с препятствием), прекращаем рывок
            isDashing = false;
            dashTarget = null;
        }
    }

    /**
     * Телепортируется за спину цели и сжигает у неё ману
     */
    private void performTeleport(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }

        // Проверяем и тратим ману
        if (!consumeMana(TELEPORT_MANA_COST)) {
            return;
        }

        // Получаем направление взгляда цели (куда она смотрит)
        Vec3 targetLookDirection = target.getLookAngle().normalize();

        // Телепортируемся за спину цели (противоположно направлению взгляда)
        Vec3 targetPos = target.position();
        Vec3 teleportDirection = targetLookDirection.scale(-1.5D); // 1.5 блока за спиной
        Vec3 teleportPos = targetPos.add(teleportDirection);

        // Находим правильную высоту (на уровне цели)
        double spawnY = target.getY();

        // Проверяем, что позиция не внутри блока
        BlockPos checkPos = BlockPos.containing(teleportPos.x, spawnY, teleportPos.z);
        if (!this.level().isEmptyBlock(checkPos)) {
            // Если блок занят, поднимаемся выше
            spawnY = target.getY() + 1.0D;
        }

        this.setPos(teleportPos.x, spawnY, teleportPos.z);
        this.hurtMarked = true;

        // Поворачиваемся лицом к цели
        this.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, target.getEyePosition());

        // Сжигаем ману у цели, если она есть
        target.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            int currentMana = mana.getMana();
            int burnedMana = Math.min(currentMana, TELEPORT_MANA_BURN);
            mana.removeMana(burnedMana);
        });

        // Анимация атаки
        this.swing(InteractionHand.MAIN_HAND);
        this.swing(InteractionHand.OFF_HAND);
    }

    /**
     * Проверяет возможность применения анти-полет способности
     */
    private boolean canUseAntiFlightAbility(LivingEntity target) {
        if (!(target instanceof Player player)) {
            return false;
        }
        if (!player.isAlive()) {
            return false;
        }
        if (!this.hasLineOfSight(player)) {
            return false;
        }
        if (!isPlayerFlightEnabled(player)) {
            return false;
        }
        double heightDifference = player.getY() - this.getY();
        if (heightDifference <= 1.0D) {
            return false; // Цель должна быть как минимум на 1 блок выше
        }
        return true;
    }

    private boolean isPlayerFlightEnabled(Player player) {
        if (Gravity.isFlyingActive(player)) {
            return true;
        }
        return player.getAbilities().flying;
    }

    private boolean performAntiFlightLightning(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (!consumeMana(ANTI_FLIGHT_MANA_COST)) {
            return false;
        }

        Vec3 basePos = target.position();
        for (int i = 0; i < ANTI_FLIGHT_LIGHTNING_COUNT; i++) {
            LightningBolt lightningBolt = EntityType.LIGHTNING_BOLT.create(serverLevel);
            if (lightningBolt == null) {
                continue;
            }
            double offsetX = (this.random.nextDouble() - 0.5D) * ANTI_FLIGHT_LIGHTNING_SPREAD;
            double offsetZ = (this.random.nextDouble() - 0.5D) * ANTI_FLIGHT_LIGHTNING_SPREAD;
            lightningBolt.setPos(basePos.x + offsetX, basePos.y, basePos.z + offsetZ);
            lightningBolt.getPersistentData().putBoolean(ANTI_FLIGHT_LIGHTNING_TAG, true);
            lightningBolt.setCause(null);
            serverLevel.addFreshEntity(lightningBolt);
        }
        return true;
    }

    /**
     * Создает 3 облака огня в случайных местах в радиусе вокруг инквизитора
     */
    private void performFireCloudAbility() {
        if (this.level().isClientSide) {
            return;
        }
        if (!consumeMana(FIRE_CLOUD_MANA_COST)) {
            return;
        }

        double centerX = this.getX();
        double centerY = this.getY();
        double centerZ = this.getZ();

        for (int i = 0; i < FIRE_CLOUD_COUNT; i++) {
            // Генерируем случайную позицию в радиусе
            double angle = this.random.nextDouble() * 2.0 * Math.PI; // Случайный угол
            double distance = this.random.nextDouble() * FIRE_CLOUD_RADIUS; // Случайное расстояние от 0 до радиуса
            double cloudX = centerX + Math.cos(angle) * distance;
            double cloudZ = centerZ + Math.sin(angle) * distance;
            
            // Используем высоту инквизитора как базовую и находим свободное место
            double cloudY = findSpawnHeight(cloudX, centerY, cloudZ);

            // Создаем облако огня
            FireCloudEntity fireCloud = new FireCloudEntity(this.level(), this, cloudX, cloudY, cloudZ);
            this.level().addFreshEntity(fireCloud);

            // Создаем эффект частиц при создании облака
            if (this.level() instanceof ServerLevel serverLevel) {
                for (int j = 0; j < 15; j++) {
                    double particleX = cloudX + (this.random.nextDouble() - 0.5) * 2;
                    double particleY = cloudY + (this.random.nextDouble() - 0.5) * 2;
                    double particleZ = cloudZ + (this.random.nextDouble() - 0.5) * 2;

                    serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        particleX, particleY, particleZ,
                        1, 0.1, 0.1, 0.1, 0.1
                    );
                }
            }
        }
    }

    private void tickActiveAbility() {
        castTicks++;
        if (activeAbility == AbilityType.SUMMON_HERO_SOUL) {
            if (castTicks == 1) {
                this.swing(InteractionHand.MAIN_HAND);
                this.swing(InteractionHand.OFF_HAND);
            }

            if (castTicks >= activeAbility.castTicks) {
                finishSummonAbility();
            }
        }
        // FIRE_WALL - моментальная способность, выполняется сразу в startAbility()
    }

    private void checkInterruptsDuringCast() {
        if (!isCastingAbility()) {
            return;
        }

        boolean hasControlEffect = this.hasEffect(ModEffects.ROAR.get()) || this.hasEffect(ModEffects.ROOT.get());
        if (hasControlEffect || damageTakenThisCast >= SUMMON_INTERRUPT_DAMAGE) {
            cancelCurrentAbility();
        }
    }

    private void tryStartRandomAbility() {
        // Проверяем блокировку способностей под эффектом SUPPRESSING_GATE
        if (isActionBlocked(HeroSouls.ActionType.ABILITY_CAST)) {
            scheduleNextAbilityRoll();
            return;
        }
        
        // Используем способности только в бою (есть цель)
        if (this.getTarget() == null || !this.getTarget().isAlive()) {
            scheduleNextAbilityRoll();
            return;
        }

        List<AbilityType> readyAbilities = collectReadyAbilities();
        if (readyAbilities.isEmpty()) {
            scheduleNextAbilityRoll();
            return;
        }

        // Гарантированно используем способность каждые 8 секунд (если есть готовая)
        // Выбор случайной способности из готовых (равномерное распределение)
        AbilityType chosen;
        if (readyAbilities.size() == 1) {
            chosen = readyAbilities.get(0);
        } else {
            // Случайный выбор из готовых способностей
            chosen = readyAbilities.get(this.random.nextInt(readyAbilities.size()));
        }

        startAbility(chosen);
    }

    private List<AbilityType> collectReadyAbilities() {
        List<AbilityType> ready = new ArrayList<>();
        for (AbilityType type : AbilityType.values()) {
            if (type != AbilityType.NONE && isAbilityReady(type)) {
                ready.add(type);
            }
        }
        return ready;
    }

    private void startAbility(AbilityType abilityType) {
        // Дополнительная проверка блокировки способностей (на случай если метод вызван напрямую)
        if (isActionBlocked(HeroSouls.ActionType.ABILITY_CAST)) {
            scheduleNextAbilityRoll();
            resetAbilityState();
            return;
        }
        
        if (abilityType == AbilityType.SUMMON_HERO_SOUL) {
            if (!consumeManaPreview(abilityType.manaCost)) {
                scheduleNextAbilityRoll();
                return;
            }

            this.activeAbility = abilityType;
            this.castTicks = 0;
            this.damageTakenThisCast = 0.0F;
            this.pendingSummonPosition = calculateSummonPosition();
            spawnTelegraph(this.pendingSummonPosition);
        } else if (abilityType == AbilityType.FIRE_WALL) {
            if (!consumeManaPreview(abilityType.manaCost)) {
                scheduleNextAbilityRoll();
                return;
            }

            // Моментальная способность, устанавливаем activeAbility и выполняем сразу
            this.activeAbility = abilityType;
            finishFireWallAbility();
        } else if (abilityType == AbilityType.FIRE_HAIL) {
            if (!consumeManaPreview(abilityType.manaCost)) {
                scheduleNextAbilityRoll();
                return;
            }

            // Моментальная способность, устанавливаем activeAbility и выполняем сразу
            this.activeAbility = abilityType;
            finishFireHailAbility();
        } else if (abilityType == AbilityType.FIREBALL_JUMP) {
            if (!consumeManaPreview(abilityType.manaCost)) {
                scheduleNextAbilityRoll();
                return;
            }

            // Моментальная способность, устанавливаем activeAbility и выполняем сразу
            this.activeAbility = abilityType;
            finishFireballJumpAbility();
        } else if (abilityType == AbilityType.HOLY_BLESSING) {
            if (!consumeManaPreview(abilityType.manaCost)) {
                scheduleNextAbilityRoll();
                return;
            }

            this.activeAbility = abilityType;
            finishHolyBlessingAbility();
        } else if (abilityType == AbilityType.FIRE_ANNIHILATION) {
            if (!consumeManaPreview(abilityType.manaCost)) {
                scheduleNextAbilityRoll();
                return;
            }

            // Моментальная способность, устанавливаем activeAbility и выполняем сразу
            this.activeAbility = abilityType;
            finishFireAnnihilationAbility();
        }
    }

    private void finishSummonAbility() {
        if (this.activeAbility != AbilityType.SUMMON_HERO_SOUL) {
            return;
        }

        removeTelegraph();

        if (!consumeMana(SUMMON_MANA_COST)) {
            scheduleNextAbilityRoll();
            resetAbilityState();
            return;
        }

        spawnHeroSouls();
        setAbilityCooldown(AbilityType.SUMMON_HERO_SOUL, SUMMON_COOLDOWN_TICKS);
        scheduleNextAbilityRoll();
        resetAbilityState();
    }

    private void finishFireWallAbility() {
        if (this.activeAbility != AbilityType.FIRE_WALL) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            scheduleNextAbilityRoll();
            resetAbilityState();
            return;
        }

        if (!consumeMana(FIRE_WALL_MANA_COST)) {
            scheduleNextAbilityRoll();
            resetAbilityState();
            return;
        }

        spawnFireWall(target);
        setAbilityCooldown(AbilityType.FIRE_WALL, FIRE_WALL_COOLDOWN_TICKS);
        scheduleNextAbilityRoll();
        resetAbilityState();
    }

    private void finishFireHailAbility() {
        if (this.activeAbility != AbilityType.FIRE_HAIL) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            scheduleNextAbilityRoll();
            resetAbilityState();
            return;
        }

        if (!consumeMana(FIRE_HAIL_MANA_COST)) {
            scheduleNextAbilityRoll();
            resetAbilityState();
            return;
        }

        spawnFireHail(target);
        setAbilityCooldown(AbilityType.FIRE_HAIL, FIRE_HAIL_COOLDOWN_TICKS);
        scheduleNextAbilityRoll();
        resetAbilityState();
    }

    private void finishFireballJumpAbility() {
        if (this.activeAbility != AbilityType.FIREBALL_JUMP) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            scheduleNextAbilityRoll();
            resetAbilityState();
            return;
        }

        if (!consumeMana(FIREBALL_JUMP_MANA_COST)) {
            scheduleNextAbilityRoll();
            resetAbilityState();
            return;
        }

        performFireballJump(target);
        setAbilityCooldown(AbilityType.FIREBALL_JUMP, FIREBALL_JUMP_COOLDOWN_TICKS);
        scheduleNextAbilityRoll();
        resetAbilityState();
    }

    private void finishHolyBlessingAbility() {
        if (this.activeAbility != AbilityType.HOLY_BLESSING) {
            return;
        }

        if (!consumeMana(BLESSING_MANA_COST)) {
            scheduleNextAbilityRoll();
            resetAbilityState();
            return;
        }

        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BLESSING_DURATION_TICKS, 0, true, true,true));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BLESSING_DURATION_TICKS, 0, true, true,true));
        this.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, BLESSING_DURATION_TICKS, 0, true, true,true));
        spawnBlessingParticles();

        setAbilityCooldown(AbilityType.HOLY_BLESSING, BLESSING_COOLDOWN_TICKS);
        scheduleNextAbilityRoll();
        resetAbilityState();
    }

    private void finishFireAnnihilationAbility() {
        if (this.activeAbility != AbilityType.FIRE_ANNIHILATION) {
            return;
        }

        LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            scheduleNextAbilityRoll();
            resetAbilityState();
            return;
        }

        if (!consumeMana(FIRE_ANNIHILATION_MANA_COST)) {
            scheduleNextAbilityRoll();
            resetAbilityState();
            return;
        }

        // Инициализируем счетчики для выстрелов с интервалом
        this.fireAnnihilationCount = FIRE_ANNIHILATION_COUNT;
        this.fireAnnihilationTarget = target;
        this.fireAnnihilationTicks = 0; // Первый выстрел сразу

        // Делаем первый выстрел сразу
        spawnFireAnnihilation(target);
        fireAnnihilationCount--;
        
        // Если остались выстрелы, устанавливаем интервал
        if (fireAnnihilationCount > 0) {
            fireAnnihilationTicks = FIRE_ANNIHILATION_INTERVAL_TICKS;
        } else {
            // Если все выстрелы сделаны, завершаем способность
            setAbilityCooldown(AbilityType.FIRE_ANNIHILATION, FIRE_ANNIHILATION_COOLDOWN_TICKS);
            scheduleNextAbilityRoll();
            resetAbilityState();
            fireAnnihilationTarget = null;
        }
    }

    private void spawnBlessingParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        for (int i = 0; i < 24; i++) {
            double angle = (2 * Math.PI * i) / 24.0;
            double radius = 1.0D + this.random.nextDouble() * 0.6D;
            double offsetX = Math.cos(angle) * radius;
            double offsetZ = Math.sin(angle) * radius;
            double offsetY = 0.4D + this.random.nextDouble() * 1.2D;
            serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    1,
                    0.0D,
                    0.02D,
                    0.0D,
                    0.0D
            );
        }
    }

    private void performFireballJump(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return;
        }

        // Проверяем, что инквизитор на земле и кулдаун прыжка прошел
        if (jumpCooldown > 0) {
            return;
        }

        // Вычисляем направление от врага (назад и вверх по диагонали)
        Vec3 toTarget = target.position().subtract(this.position()).normalize();
        // Направление назад от врага
        Vec3 backwardDirection = toTarget.scale(-1.0);
        
        // Прыжок назад и вверх (диагонально)
        double backwardPower = BACKWARD_STRENGTH;
        double upwardPower = UPWARD_STRENGTH;
        
        // Добавляем движение к текущему (как в LanserEntity)
        Vec3 currentMovement = this.getDeltaMovement();
        Vec3 jumpMovement = new Vec3(
            currentMovement.x + backwardDirection.x * backwardPower,
            currentMovement.y + upwardPower,
            currentMovement.z + backwardDirection.z * backwardPower
        );
        
        // Применяем прыжок
        this.setDeltaMovement(jumpMovement);
        this.hasImpulse = true;
        
        // Устанавливаем кулдаун прыжка
        this.jumpCooldown = JUMP_COOLDOWN_TICKS;


        this.fireballJumpTicks = 15;
        this.fireballJumpTarget = target;
    }

    /**
     * Обрабатывает задержку выстрела после прыжка
     */
    private void tickFireballJump() {
        if (fireballJumpTicks > 0 && fireballJumpTarget != null) {
            fireballJumpTicks--;
            if (fireballJumpTicks <= 0) {
                if (this.isAlive() && fireballJumpTarget.isAlive()) {
                    shootFireball(fireballJumpTarget);
                }
                fireballJumpTarget = null;
            }
        }
    }

    /**
     * Обрабатывает выстрелы FireAnnihilation с интервалом
     */
    private void tickFireAnnihilation() {
        if (fireAnnihilationTicks > 0 && fireAnnihilationTarget != null && fireAnnihilationCount > 0) {
            fireAnnihilationTicks--;
            if (fireAnnihilationTicks <= 0) {
                if (this.isAlive() && fireAnnihilationTarget.isAlive()) {
                    spawnFireAnnihilation(fireAnnihilationTarget);
                    fireAnnihilationCount--;
                    
                    // Если остались выстрелы, устанавливаем следующий интервал
                    if (fireAnnihilationCount > 0) {
                        fireAnnihilationTicks = FIRE_ANNIHILATION_INTERVAL_TICKS;
                    } else {
                        // Все выстрелы сделаны, завершаем способность
                        setAbilityCooldown(AbilityType.FIRE_ANNIHILATION, FIRE_ANNIHILATION_COOLDOWN_TICKS);
                        scheduleNextAbilityRoll();
                        resetAbilityState();
                        fireAnnihilationTarget = null;
                    }
                } else {
                    // Цель мертва или инквизитор мертв, завершаем
                    fireAnnihilationTarget = null;
                    fireAnnihilationCount = 0;
                    fireAnnihilationTicks = 0;
                    scheduleNextAbilityRoll();
                    resetAbilityState();
                }
            }
        }
    }

    private void shootFireball(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel) || target == null || !target.isAlive()) {
            return;
        }

        // Используем правильный конструктор: FireBallEntity(Level level, LivingEntity owner)
        FireBallEntity fireball = new FireBallEntity(serverLevel, this);

        // Позиция выстрела (немного впереди инквизитора)
        Vec3 lookDirection = this.getLookAngle().normalize();
        Vec3 shootPos = this.getEyePosition().add(lookDirection.scale(0.5));
        fireball.setPos(shootPos.x, shootPos.y, shootPos.z);

        // Вычисляем направление к цели
        Vec3 targetPos = target.getEyePosition();
        Vec3 direction = targetPos.subtract(shootPos).normalize();

        // Скорость выстрела (немного больше для быстрого попадания)
        float speed = 1.5f;
        float inaccuracy = 0.05f; // Добавляем небольшую неточность для реалистичности

        // Выстреливаем
        fireball.shoot(direction.x, direction.y, direction.z, speed, inaccuracy);

        // Добавляем визуальные/звуковые эффекты
        serverLevel.addFreshEntity(fireball);
        serverLevel.playSound(null,
                shootPos.x, shootPos.y, shootPos.z,
                SoundEvents.BLAZE_SHOOT,
                SoundSource.HOSTILE,
                1.0f,
                1.0f + (random.nextFloat() - random.nextFloat()) * 0.2f
        );
    }

    /**
     * Проверяет необходимость прыжка вверх и выполняет его
     */
    private void checkAndPerformVerticalJump(LivingEntity target) {
        if (verticalJumpCooldown > 0 || !this.onGround()) return;

        double targetY = target.getY();
        double thisY = this.getY();
        double heightDifference = targetY - thisY;

        // Проверяем горизонтальную дистанцию до цели
        double horizontalDistance = this.distanceTo(target);

        // Если цель выше и разница значительная, и находится на близкой дистанции, пытаемся прыгнуть
        if (heightDifference > VERTICAL_JUMP_THRESHOLD &&
                horizontalDistance <= VERTICAL_JUMP_RANGE) {
            // Проверяем, есть ли видимость между нами и целью
            if (this.hasLineOfSight(target)) {
                performVerticalJump();
            }
        }
    }

    /**
     * Выполняет прыжок вверх (выше чем у Leonid)
     */
    private void performVerticalJump() {
        if (this.onGround() && verticalJumpCooldown <= 0) {
            // Прыгаем вверх с силой выше чем у Leonid
            double jumpPower = VERTICAL_JUMP_MIN_POWER + (this.random.nextDouble() * (VERTICAL_JUMP_MAX_POWER - VERTICAL_JUMP_MIN_POWER));
            this.setDeltaMovement(this.getDeltaMovement().add(0, jumpPower, 0));
            this.hasImpulse = true;
            verticalJumpCooldown = VERTICAL_JUMP_COOLDOWN_TICKS;

            // Проигрываем звук прыжка
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_STRONG, net.minecraft.sounds.SoundSource.HOSTILE, 0.5F, 1.2F);
        }
    }

    private void spawnFireWall(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 wallPos = calculateFireWallPosition(target);
        if (wallPos == null) {
            return;
        }

        // Вычисляем направление стены: перпендикулярно линии инквизитор-цель
        Vec3 toTarget = target.position().subtract(this.position()).normalize();
        float yaw = (float) Math.toDegrees(Math.atan2(-toTarget.x, toTarget.z));

        // Поворачиваем на 90 градусов, чтобы стена была перпендикулярна
        yaw += 90.0F;

        FireWallEntity fireWall = new FireWallEntity(serverLevel, this, wallPos, yaw);
        serverLevel.addFreshEntity(fireWall);
    }

    private void spawnFireHail(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 targetPos = target.position();
        double centerX = targetPos.x;
        double centerY = targetPos.y + 8.0D; // 8 блоков над целью
        double centerZ = targetPos.z;

        // Создаем снаряды в круге радиусом 2 блока
        for (int i = 0; i < FIRE_HAIL_COUNT; i++) {
            // Равномерное распределение по кругу
            double angle = (2.0 * Math.PI * i) / FIRE_HAIL_COUNT;
            double offsetX = Math.cos(angle) * FIRE_HAIL_RADIUS;
            double offsetZ = Math.sin(angle) * FIRE_HAIL_RADIUS;

            double spawnX = centerX + offsetX;
            double spawnY = centerY;
            double spawnZ = centerZ + offsetZ;

            // Создаем снаряд
            FireHailEntity fireHail = new FireHailEntity(serverLevel, target);
            fireHail.setPos(spawnX, spawnY, spawnZ);
            fireHail.setOwner(this); // Устанавливаем владельца
            serverLevel.addFreshEntity(fireHail);
        }

        // Добавляем снаряд в центре (прямо под целью)
        FireHailEntity centerFireHail = new FireHailEntity(serverLevel, target);
        centerFireHail.setPos(centerX, centerY, centerZ);
        centerFireHail.setOwner(this);
        serverLevel.addFreshEntity(centerFireHail);
    }

    private void spawnFireAnnihilation(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel) || target == null || !target.isAlive()) {
            return;
        }

        // Вычисляем направление взгляда инквизитора
        Vec3 look = this.getLookAngle().normalize();

        // Позиция спавна спереди инквизитора (как в FireAnnihilation.java)
        double x = this.getX() + look.x * FIRE_ANNIHILATION_SPAWN_DISTANCE;
        double y = this.getEyeY() - 0.2;
        double z = this.getZ() + look.z * FIRE_ANNIHILATION_SPAWN_DISTANCE;

        // Создаем FireAnnihilationEntity
        FireAnnihilationEntity fireAnnihilation = new FireAnnihilationEntity(serverLevel, this, x, y, z);
        serverLevel.addFreshEntity(fireAnnihilation);
    }

    private Vec3 calculateFireWallPosition(LivingEntity target) {
        // Позиция цели
        Vec3 targetPos = target.position();

        // Направление от цели к инквизитору (за спиной цели)
        Vec3 toInquisitor = this.position().subtract(targetPos).normalize();

        // Спавним стену в 1 блоке от цели (за спиной)
        Vec3 wallPos = targetPos.add(toInquisitor.scale(1.0D));

        // Используем высоту цели как базовую и находим свободное место
        double spawnY = findSpawnHeight(wallPos.x, target.getY(), wallPos.z);

        return new Vec3(wallPos.x, spawnY, wallPos.z);
    }

    private void spawnHeroSouls() {
        if (!(this.level() instanceof ServerLevel serverLevel) || pendingSummonPosition == null) {
            return;
        }

        Optional<HeroSoulOption> option = rollHeroSoul(serverLevel.random);
        if (option.isEmpty()) {
            return;
        }

        HeroSoulOption selected = option.get();
        for (int i = 0; i < selected.count(); i++) {
            spawnSingleHeroSoul(serverLevel, selected);
        }
    }

    private void spawnSingleHeroSoul(ServerLevel serverLevel, HeroSoulOption option) {
        EntityType<? extends Mob> type = option.entity().get();
        if (type == ModEntities.INQUISITOR.get()) {
            return;
        }
        Mob heroSoul = type.create(serverLevel);
        if (heroSoul == null) {
            return;
        }

        Vec3 spawnPos = offsetSummonPosition();
        heroSoul.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, this.getYRot(), 0.0F);

        heroSoul.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(heroSoul.blockPosition()),
                MobSpawnType.MOB_SUMMONED, (SpawnGroupData) null, null);
        heroSoul.setTarget(this.getTarget());
        heroSoul.setPersistenceRequired();

        // Очищаем все стандартные цели атаки и добавляем только нашу
        heroSoul.targetSelector.removeAllGoals(goal -> true);
        heroSoul.targetSelector.addGoal(0, new InquisitorGuardGoal(heroSoul, this));

        // Используем только наш тег, без LordOfSouls
        heroSoul.getPersistentData().putUUID("inquisitor_owner", this.getUUID());

        serverLevel.addFreshEntity(heroSoul);
    }

    private Vec3 offsetSummonPosition() {
        RandomSource randomSource = this.getRandom();
        double radius = 1.5D + randomSource.nextDouble() * 0.5D;
        double angle = randomSource.nextDouble() * Math.PI * 2;
        double xOffset = Math.cos(angle) * radius;
        double zOffset = Math.sin(angle) * radius;
        Vec3 base = this.pendingSummonPosition != null ? this.pendingSummonPosition : this.position();
        return new Vec3(base.x + xOffset, base.y, base.z + zOffset);
    }

    private void cancelCurrentAbility() {
        removeTelegraph();
        resetAbilityState();
        scheduleNextAbilityRoll();
    }

    private void resetAbilityState() {
        this.activeAbility = AbilityType.NONE;
        this.castTicks = 0;
        this.damageTakenThisCast = 0.0F;
        this.pendingSummonPosition = null;
        // Сбрасываем состояние FireAnnihilation
        this.fireAnnihilationTicks = 0;
        this.fireAnnihilationCount = 0;
        this.fireAnnihilationTarget = null;
    }

    private void scheduleNextAbilityRoll() {
        this.nextAbilityRollTick = this.tickCount + GLOBAL_COOLDOWN_TICKS;
    }

    private void setAbilityCooldown(AbilityType abilityType, int duration) {
        abilityCooldowns.put(abilityType, this.tickCount + duration);
    }

    private boolean isAbilityReady(AbilityType abilityType) {
        if (abilityType == AbilityType.NONE) {
            return false;
        }
        int readyTick = abilityCooldowns.getOrDefault(abilityType, 0);
        return this.tickCount >= readyTick && this.getMana() >= abilityType.manaCost;
    }

    private boolean consumeMana(int amount) {
        if (this.getMana() < amount) {
            return false;
        }
        setMana(this.getMana() - amount);
        return true;
    }

    private boolean consumeManaPreview(int amount) {
        return this.getMana() >= amount;
    }

    private void addMana(int amount) {
        setMana(this.getMana() + amount);
    }

    private void setMana(int value) {
        this.entityData.set(DATA_MANA, Mth.clamp(value, 0, MAX_MANA));
    }

    public int getMana() {
        return this.entityData.get(DATA_MANA);
    }

    private boolean isCastingAbility() {
        return this.activeAbility != AbilityType.NONE;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        LivingEntity previousTarget = this.getTarget();
        boolean friendlySource = isFriendlySource(source);

        // Сохраняем здоровье до получения урона
        float healthBefore = this.getHealth();
        boolean result = super.hurt(source, amount);

        if (friendlySource && result) {
            // Не агримся на своих призванных существ
            if (previousTarget != null && previousTarget.isAlive() && previousTarget != this) {
                this.setTarget(previousTarget);
            } else if (this.getTarget() != null && isMySummon(this.getTarget())) {
                this.setTarget(null);
            }
            this.setLastHurtByMob(null);
        } else if (result && !friendlySource) {
            // Если получили урон от игрока, устанавливаем его как цель
            Entity attacker = source.getEntity();
            if (attacker instanceof Player player && player.isAlive()) {
                this.setTarget(player);
                this.setLastHurtByMob(player);
            } else if (attacker instanceof LivingEntity livingAttacker && livingAttacker.isAlive()) {
                // Также атакуем других мобов, если они нанесли урон
                this.setTarget(livingAttacker);
                this.setLastHurtByMob(livingAttacker);
            }
        }

        if (result && isCastingAbility()) {
            // Вычисляем реальный урон (с учетом брони, защиты и т.д.)
            float healthAfter = this.getHealth();
            float actualDamage = healthBefore - healthAfter;
            this.damageTakenThisCast += actualDamage;
        }
        return result;
    }

    private boolean isFriendlySource(DamageSource source) {
        if (source == null) {
            return false;
        }
        Entity entity = source.getEntity();
        if (entity != null && isMySummon(entity)) {
            return true;
        }
        Entity direct = source.getDirectEntity();
        return direct != null && isMySummon(direct);
    }

    private boolean isMySummon(Entity entity) {
        if (entity == null) {
            return false;
        }
        if (entity.getPersistentData().contains("inquisitor_owner")) {
            try {
                UUID uuid = entity.getPersistentData().getUUID("inquisitor_owner");
                return uuid != null && uuid.equals(this.getUUID());
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    /**
     * Проверяет потерю HP и переключается на Holy_bayonets после потери 50 HP
     */
    private void checkAndSwitchWeapon() {
        if (hasSwitchedToHolyBayonets) {
            return; // Уже переключились
        }

        float currentHealth = this.getHealth();
        float healthLost = initialHealth - currentHealth;

        if (healthLost >= HP_THRESHOLD_FOR_WEAPON_SWITCH) {
            // Переключаемся на Holy_bayonets в обеих руках
            this.setItemSlot(EquipmentSlot.MAINHAND, ModItems.HOLY_BAYONETS.get().getDefaultInstance());
            this.setItemSlot(EquipmentSlot.OFFHAND, ModItems.HOLY_BAYONETS.get().getDefaultInstance());
            hasSwitchedToHolyBayonets = true;
        }
    }

    /**
     * Находит правильную высоту для спавна сущности относительно базовой высоты
     * Ищет свободное место рядом с базовой высотой, а не на поверхности
     */
    private double findSpawnHeight(double x, double baseY, double z) {
        BlockPos checkPos = BlockPos.containing(x, baseY, z);
        
        // Начинаем с базовой высоты и ищем свободное место
        // Проверяем вверх и вниз от базовой высоты (до 5 блоков в каждую сторону)
        int baseBlockY = BlockPos.containing(x, baseY, z).getY();
        
        // Сначала проверяем саму базовую высоту
        if (isValidSpawnHeight(x, baseBlockY, z)) {
            return baseBlockY + 0.5;
        }
        
        // Ищем вверх от базовой высоты
        for (int offset = 1; offset <= 5; offset++) {
            int checkY = baseBlockY + offset;
            if (isValidSpawnHeight(x, checkY, z)) {
                return checkY + 0.5;
            }
        }
        
        // Ищем вниз от базовой высоты
        for (int offset = 1; offset <= 5; offset++) {
            int checkY = baseBlockY - offset;
            if (isValidSpawnHeight(x, checkY, z)) {
                return checkY + 0.5;
            }
        }
        
        // Если ничего не нашли, возвращаем базовую высоту
        return baseY;
    }
    
    /**
     * Проверяет, является ли позиция валидной для спавна (блок пустой и над ним есть место)
     */
    private boolean isValidSpawnHeight(double x, int y, double z) {
        BlockPos pos = BlockPos.containing(x, y, z);
        BlockPos abovePos = pos.above();
        
        // Проверяем, что текущий блок пустой или проходимый, и блок сверху тоже пустой
        return this.level().isEmptyBlock(pos) && this.level().isEmptyBlock(abovePos);
    }

    private Vec3 calculateSummonPosition() {
        Vec3 look = this.getLookAngle();
        Vec3 forward = this.position().add(look.normalize().scale(3.0D));
        // Используем высоту инквизитора как базовую и находим свободное место
        double spawnY = findSpawnHeight(forward.x, this.getY(), forward.z);
        return new Vec3(forward.x, spawnY, forward.z);
    }

    private void spawnTelegraph(Vec3 position) {
        if (this.level().isClientSide || position == null) {
            return;
        }

        removeTelegraph();

        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), position.x, position.y, position.z);
        cloud.setRadius(2.2F);
        cloud.setRadiusPerTick(0.0F);
        cloud.setDuration(SUMMON_CAST_TICKS);
        cloud.setFixedColor(0xAA0000);
        cloud.setWaitTime(0);
        cloud.setOwner(this);
        cloud.setParticle(ParticleTypes.FLAME);

        this.level().addFreshEntity(cloud);
        this.telegraphCloud = cloud;
    }

    private void removeTelegraph() {
        if (this.telegraphCloud != null) {
            this.telegraphCloud.discard();
            this.telegraphCloud = null;
        }
    }

    private static List<HeroSoulOption> buildHeroSoulPool() {
        List<HeroSoulOption> list = new ArrayList<>();
        list.add(new HeroSoulOption(ModEntities.LANSER, 1, 10));
        list.add(new HeroSoulOption(ModEntities.LEONID, 1, 10));
        list.add(new HeroSoulOption(ModEntities.BERSERK, 1, 10));
        list.add(new HeroSoulOption(ModEntities.ARCHER, 1, 10));
        list.add(new HeroSoulOption(ModEntities.ASSASSIN, 3, 10));
        list.add(new HeroSoulOption(ModEntities.RED_BERSERK, 1, 1));
        return List.copyOf(list);
    }

    private Optional<HeroSoulOption> rollHeroSoul(RandomSource random) {
        if (HERO_SOUL_POOL.isEmpty() || HERO_SOUL_TOTAL_WEIGHT <= 0) {
            return Optional.empty();
        }

        int roll = random.nextInt(HERO_SOUL_TOTAL_WEIGHT) + 1;
        int cumulative = 0;
        for (HeroSoulOption option : HERO_SOUL_POOL) {
            cumulative += option.weight();
            if (roll <= cumulative) {
                return Optional.of(option);
            }
        }
        return Optional.empty();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("Mana", this.getMana());
        compound.putInt("NextAbilityRoll", this.nextAbilityRollTick);
        compound.putInt("DashCooldown", this.dashCooldown);
        compound.putInt("TeleportCooldown", this.teleportCooldown);
        compound.putInt("FireCloudCooldown", this.fireCloudCooldown);
        compound.putInt("JumpCooldown", this.jumpCooldown);
        compound.putInt("VerticalJumpCooldown", this.verticalJumpCooldown);
        compound.putFloat("InitialHealth", this.initialHealth);
        compound.putBoolean("HasSwitchedToHolyBayonets", this.hasSwitchedToHolyBayonets);
        CompoundTag cooldowns = new CompoundTag();
        for (AbilityType type : abilityCooldowns.keySet()) {
            cooldowns.putInt(type.name(), abilityCooldowns.getInt(type));
        }
        compound.put("AbilityCooldowns", cooldowns);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.setMana(compound.getInt("Mana"));
        this.nextAbilityRollTick = compound.getInt("NextAbilityRoll");
        this.dashCooldown = compound.getInt("DashCooldown");
        this.teleportCooldown = compound.getInt("TeleportCooldown");
        this.fireCloudCooldown = compound.getInt("FireCloudCooldown");
        this.jumpCooldown = compound.getInt("JumpCooldown");
        this.verticalJumpCooldown = compound.getInt("VerticalJumpCooldown");
        if (compound.contains("InitialHealth")) {
            this.initialHealth = compound.getFloat("InitialHealth");
        } else {
            this.initialHealth = this.getMaxHealth();
        }
        this.hasSwitchedToHolyBayonets = compound.getBoolean("HasSwitchedToHolyBayonets");
        this.activeAbility = AbilityType.NONE;
        this.castTicks = 0;
        this.damageTakenThisCast = 0.0F;
        this.pendingSummonPosition = null;
        this.abilityCooldowns.clear();

        if (compound.contains("AbilityCooldowns")) {
            CompoundTag cooldowns = compound.getCompound("AbilityCooldowns");
            for (AbilityType type : AbilityType.values()) {
                if (cooldowns.contains(type.name())) {
                    abilityCooldowns.put(type, cooldowns.getInt(type.name()));
                }
            }
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, spawnGroupData, dataTag);
        this.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            mana.setMaxMana(MAX_MANA);
            mana.setMana(MAX_MANA);
        });
        this.nextAbilityRollTick = this.tickCount + GLOBAL_COOLDOWN_TICKS;
        this.initialHealth = this.getMaxHealth(); // Инициализируем начальное здоровье
        this.hasSwitchedToHolyBayonets = false; // Сбрасываем флаг переключения оружия

        AttributeInstance attribute = this.getAttribute(ModAttributes.BONUS_ARMOR.get());
        attribute.setBaseValue(BONUS_ARMOR);
        return data;
    }

    private record HeroSoulOption(RegistryObject<? extends EntityType<? extends Mob>> entity, int count, int weight) {}

    private static class InquisitorMeleeAttackGoal extends Goal {
        private final InquisitorEntity inquisitor;
        private final double speedModifier;
        private int attackCooldown;
        private boolean attackWithMainHand = true;

        public InquisitorMeleeAttackGoal(InquisitorEntity inquisitor, double speedModifier) {
            this.inquisitor = inquisitor;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (inquisitor.isCastingAbility() || inquisitor.isActionBlocked(HeroSouls.ActionType.MELEE_ATTACK)) {
                return false;
            }
            LivingEntity target = inquisitor.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            if (inquisitor.isCastingAbility() || inquisitor.isActionBlocked(HeroSouls.ActionType.MELEE_ATTACK)) {
                return false;
            }
            LivingEntity target = inquisitor.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = inquisitor.getTarget();
            if (target == null) {
                return;
            }

            // Защита от самоатаки
            if (target == inquisitor) {
                inquisitor.setTarget(null);
                return;
            }

            inquisitor.getNavigation().moveTo(target, speedModifier);
            inquisitor.getLookControl().setLookAt(target, 30.0F, 30.0F);

            double distanceSqr = inquisitor.distanceToSqr(target);
            if (distanceSqr <= ATTACK_RANGE * ATTACK_RANGE && inquisitor.hasLineOfSight(target)) {
                if (attackCooldown <= 0) {
                    InteractionHand hand = InteractionHand.MAIN_HAND;
                    if (inquisitor.canUseBothHands()) {
                        hand = attackWithMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                        attackWithMainHand = !attackWithMainHand;
                    }

                    inquisitor.swing(hand);
                    // Используем doHurtTarget() для правильной атаки с учетом оружия и зачарований
                    inquisitor.doHurtTarget(target);
                    attackCooldown = ATTACK_COOLDOWN_TICKS;
                } else {
                    attackCooldown--;
                }
            } else {
                attackCooldown = 0;
            }
        }
    }
    @Override
    protected void dropEquipment() {
        // Не дропаем экипировку при смерти
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        Random random = new Random(); // Генератор случайных чисел
        double chanceCircuit = 0.25;
        double chanceCross = 0.23;
        double chanceAnni = 0.7;
        double chanceAncient = 0.5;

        if (random.nextDouble() < chanceCircuit) {
            int circuitCount = random.nextInt(30, 51);
            this.spawnAtLocation(new ItemStack(ModItems.MAGIC_CIRCUIT.get(), circuitCount));
        }
        if (random.nextDouble() < chanceCross) {
            this.spawnAtLocation(new ItemStack(ModItems.CROSS.get(), 1));
        }
        if (random.nextDouble() < chanceAnni) {
            this.spawnAtLocation(new ItemStack(ModItems.FIRE_ANNIHILATION.get(), 1));
        }
        if (random.nextDouble() < chanceAncient) {
            this.spawnAtLocation(new ItemStack(ModItems.ANCIENT_SCROLL.get(), 1));
        }
    }

    @Override
    protected void dropExperience() {
        if (!this.level().isClientSide) {
            this.level().addFreshEntity(new ExperienceOrb(
                    this.level(),
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    1000 // Количество опыта
            ));
        }
    }
}

