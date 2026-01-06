package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.entity.ai.BreakBlockGoal;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.capability.mana.ManaProvider;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.ForgeMod;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;
import java.util.Random;
import net.artur.nacikmod.entity.projectiles.ShamakEntity;

public class AssassinEntity extends HeroSouls {
    private static final double ATTACK_RANGE = 2.5D;
    private int attackCooldown = 0;
    private static final int ATTACK_COOLDOWN_TICKS = 11; // Быстрые атаки
    private int stealthCooldown = 0;
    private static final int STEALTH_COOLDOWN = 200; // 10 секунд
    private boolean isStealthed = false;
    private static final double STEALTH_SPEED_BONUS = 0.25; // Бонус скорости в стелсе
    private static int BONUS_ARMOR = 6;

    // Переменные для невидимости
    private boolean isInvisibleAbility = false; // флаг именно способности ассасина
    private int invisibilityDuration = 0;
    private static final int INVISIBILITY_DURATION_TICKS = 100; // 5 секунд (20 тиков * 5)
    private int invisibilityCooldown = 0;
    private static final int INVISIBILITY_COOLDOWN_TICKS = 200;
    private static final double INVISIBILITY_DISTANCE = 5.0; // Дистанция в невидимости

    // Переменные для телепортации и атаки из невидимости
    private LivingEntity invisibilityTarget = null; // Цель для атаки из невидимости
    private boolean shouldTeleportAttack = false; // Флаг для телепортации и атаки

    // Константы для маны и способности Shamak
    private static final int MAX_MANA = 2000;
    private static final int SHAMAK_MANA_COST = 500;
    private boolean hasShamakAbility = false; // Флаг наличия способности Shamak
    private boolean hasUsedShamak = false; // Флаг использования Shamak в бою

    // Константы для способности клонирования после смерти
    private static final double CLONE_SPAWN_RADIUS = 2.0;
    private boolean hasCloneAbility = false; // Флаг наличия способности клонирования


    public AssassinEntity(EntityType<? extends HeroSouls> entityType, Level level) {
        super(entityType, level);
        this.setCanUseBothHands(true);
        this.setAttackRange(ATTACK_RANGE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(ModAttributes.BONUS_ARMOR.get(), BONUS_ARMOR)
                .add(Attributes.ARMOR, 9)
                .add(Attributes.ARMOR_TOUGHNESS, 5)
                .add(Attributes.MAX_HEALTH, 55.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.445)
                .add(Attributes.FOLLOW_RANGE, 35.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                .add(ForgeMod.SWIM_SPEED.get(), 2);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new BreakBlockGoal(this));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true)); // Открытие дверей во время боя
        this.goalSelector.addGoal(3, new AssassinInvisibilityGoal(this, 1.0D)); // Goal для невидимости
        this.goalSelector.addGoal(4, new AssassinMeleeAttackGoal(this, 1.2D)); // Обычная атака
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(AssassinEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, this::isValidTarget));
    }

    private boolean isValidTarget(LivingEntity entity) {
        return !(entity instanceof Animal) &&
                !(entity instanceof WaterAnimal) &&
                !(entity instanceof AssassinEntity);
    }

    @Override
    public void tick() {
        super.tick();

        // Обновляем кулдауны
        if (attackCooldown > 0) attackCooldown--;
        if (stealthCooldown > 0) stealthCooldown--;
        if (invisibilityCooldown > 0) invisibilityCooldown--;

        // Логика невидимости способности
        if (isInvisibleAbility) {
            invisibilityDuration--;

            // Если прошло 5 секунд (100 тиков) и нужно телепортироваться к врагу
            if (invisibilityDuration == 0 && shouldTeleportAttack && invisibilityTarget != null && invisibilityTarget.isAlive()) {
                performTeleportAttack();
            }

            if (invisibilityDuration <= 0) {
                deactivateInvisibility();
            }
        }

        // Если невидимость есть, но не через способность — просто игнорируем
        // (например, зелье или команда /effect)
        if (!isInvisibleAbility && this.hasEffect(MobEffects.INVISIBILITY)) {
            // Тут можно, если хочешь, обновлять флаг, но я оставлю только эффект
        }

        // Логика стелса (только если не в невидимости способности)
        LivingEntity target = this.getTarget();
        if (target != null && stealthCooldown <= 0 && !isInvisibleAbility) {
            double distance = this.distanceToSqr(target);
            if (distance > 16.0 && distance < 64.0) { // Активируем стелс на среднем расстоянии
                activateStealth();
            } else if (distance <= 8.0) { // Деактивируем стелс при близком контакте
                deactivateStealth();
            }
        }

        // Логика способности Shamak
        if (hasShamakAbility && !hasUsedShamak && !this.level().isClientSide) {
            float healthPercentage = (this.getHealth() / this.getMaxHealth()) * 100;
            
            // Если здоровье меньше 50% и есть достаточно маны
            if (healthPercentage <= 50) {
                this.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                    if (mana.getMana() >= SHAMAK_MANA_COST) {
                        spawnShamak();
                        mana.removeMana(SHAMAK_MANA_COST);
                        hasUsedShamak = true;
                    }
                });
            }
        }

        // Сбрасываем флаг Shamak если здоровье восстановилось выше 50%
        if (hasShamakAbility && hasUsedShamak) {
            float healthPercentage = (this.getHealth() / this.getMaxHealth()) * 100;
            if (healthPercentage > 50) {
                hasUsedShamak = false;
            }
        }

        // Если ассасин в невидимости после Shamak, не позволяем активировать обычную невидимость
        if (isInvisibleAbility && shouldTeleportAttack) {
            // Блокируем обычную активацию невидимости
            return;
        }

        // Ассасин иммунен к яду, иссушению и Strong Poison - просто удаляем эти эффекты
        if (this.hasEffect(MobEffects.POISON)) {
            this.removeEffect(MobEffects.POISON);
        }
        if (this.hasEffect(MobEffects.WITHER)) {
            this.removeEffect(MobEffects.WITHER);
        }
        // Удаляем Strong Poison по его ID
        this.getActiveEffects().removeIf(effect -> 
            effect.getEffect().getDescriptionId().equals("effect.nacikmod.strong_poison")
        );
    }


    private void activateStealth() {
        if (!isStealthed) {
            isStealthed = true;
            // Увеличиваем скорость в стелсе
            AttributeInstance speedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.setBaseValue(0.45 + STEALTH_SPEED_BONUS);
            }
        }
    }

    private void deactivateStealth() {
        if (isStealthed) {
            isStealthed = false;
            stealthCooldown = STEALTH_COOLDOWN;
            // Возвращаем нормальную скорость
            AttributeInstance speedAttribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.setBaseValue(0.45);
            }
        }
    }

    public boolean isStealthed() {
        return isStealthed;
    }

    private void activateInvisibility() {
        // Если ассасин в невидимости после Shamak, не позволяем активировать обычную невидимость
        if (isInvisibleAbility && shouldTeleportAttack) {
            return;
        }
        
        if (!isInvisibleAbility && invisibilityCooldown <= 0) {
            isInvisibleAbility = true;
            invisibilityDuration = INVISIBILITY_DURATION_TICKS;
            this.addEffect(new MobEffectInstance(
                    MobEffects.INVISIBILITY,
                    INVISIBILITY_DURATION_TICKS,
                    0,
                    false,
                    false
            ));

            // Сохраняем текущую цель для атаки из невидимости
            invisibilityTarget = this.getTarget();
            // НЕ сбрасываем цель - ассасин должен сохранять её для соблюдения дистанции
            shouldTeleportAttack = true;
        }
    }


    private void deactivateInvisibility() {
        if (isInvisibleAbility) {
            isInvisibleAbility = false;
            invisibilityCooldown = INVISIBILITY_COOLDOWN_TICKS;
            shouldTeleportAttack = false;
            invisibilityTarget = null;

            // Убираем эффект только если он был наложен способностью
            MobEffectInstance effect = this.getEffect(MobEffects.INVISIBILITY);
            if (effect != null && effect.getDuration() <= INVISIBILITY_DURATION_TICKS) {
                this.removeEffect(MobEffects.INVISIBILITY);
            }
        }
    }

    private void performTeleportAttack() {
        // 1. ПЕРВАЯ ПРОВЕРКА: Если цель исчезла до начала выполнения метода
        if (invisibilityTarget == null || !invisibilityTarget.isAlive()) {
            this.shouldTeleportAttack = false;
            this.invisibilityTarget = null;
            return;
        }

        // Телепортируемся к врагу
        Vec3 targetPos = invisibilityTarget.position();
        this.setPos(targetPos.x, targetPos.y, targetPos.z);

        // Устанавливаем врага как цель
        this.setTarget(invisibilityTarget);

        // Наносим дополнительный урон
        float baseDamage = (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float totalDamage = baseDamage + 20.0f;

        // 2. ВТОРАЯ ПРОВЕРКА: Перед наложением эффектов
        // Это то самое место, где случился твой NPE в логах (invisibilityTarget.addEffect)
        if (invisibilityTarget != null && invisibilityTarget.isAlive()) {
            invisibilityTarget.hurt(this.damageSources().mobAttack(this), totalDamage);

            invisibilityTarget.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0, false, false));
            invisibilityTarget.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0, false, false));
        }

        // Анимация атаки
        this.swing(InteractionHand.MAIN_HAND);
        if (!this.getOffhandItem().isEmpty()) {
            this.swing(InteractionHand.OFF_HAND);
        }

        // Сбрасываем флаги
        this.shouldTeleportAttack = false;
        this.invisibilityTarget = null;
    }

    private void spawnShamak() {
        if (this.level() instanceof ServerLevel serverLevel) {
            // Создаем Shamak в позиции ассасина
            ShamakEntity shamak = new ShamakEntity(
                    this.level(),
                    this, // владелец
                    this.getX(),
                    this.getY(),
                    this.getZ()
            );
            
            serverLevel.addFreshEntity(shamak);
            
            // После применения Shamak ассасин автоматически уходит в невидимость
            // игнорируя перезарядку
            activateInvisibilityAfterShamak();
        }
    }

    private void spawnAssassinClones() {
        if (this.level() instanceof ServerLevel serverLevel) {
            // Получаем позицию ассасина
            double x = this.getX();
            double y = this.getY();
            double z = this.getZ();

            // Спавним 2 клона ассасина
            spawnAssassinCloneAt(x - CLONE_SPAWN_RADIUS, y, z - CLONE_SPAWN_RADIUS, serverLevel);
            spawnAssassinCloneAt(x + CLONE_SPAWN_RADIUS, y, z + CLONE_SPAWN_RADIUS, serverLevel);
        }
    }

    private void spawnAssassinCloneAt(double x, double y, double z, ServerLevel serverLevel) {
        AssassinEntity clone = ModEntities.ASSASSIN.get().create(serverLevel);
        if (clone != null) {
            clone.setPos(x, y, z);
            clone.setTarget(this.getTarget()); // Устанавливаем цель клона
            
            // Вызываем finalizeSpawn для инициализации клона
            clone.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(clone.blockPosition()),
                    MobSpawnType.MOB_SUMMONED, null, null);
            
            serverLevel.addFreshEntity(clone);
        }
    }

    /**
     * Активирует невидимость после применения Shamak, игнорируя перезарядку
     * и обычные ограничения невидимости
     */
    private void activateInvisibilityAfterShamak() {
        // Принудительно активируем невидимость после Shamak
        isInvisibleAbility = true;
        invisibilityDuration = INVISIBILITY_DURATION_TICKS;
        this.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY,
                INVISIBILITY_DURATION_TICKS,
                0,
                false,
                false
        ));

        // Сохраняем текущую цель для атаки из невидимости
        invisibilityTarget = this.getTarget();
        // НЕ сбрасываем цель - ассасин должен сохранять её для соблюдения дистанции
        shouldTeleportAttack = true;
        
        // Сбрасываем кулдаун невидимости
        invisibilityCooldown = 0;
    }


    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Ассасин получает меньше урона в стелсе
        if (isStealthed) {
            amount *= 0.6f; // 40% снижение урона
        }

        boolean wasInvisible = this.hasEffect(MobEffects.INVISIBILITY);

        // Если ассасин уже невидим, снимаем эффект невидимости при получении урона
        if (wasInvisible) {
            this.removeEffect(MobEffects.INVISIBILITY);
            isInvisibleAbility = false;
            invisibilityCooldown = INVISIBILITY_COOLDOWN_TICKS;
            shouldTeleportAttack = false;
            invisibilityTarget = null;
        }

        boolean isHurt = super.hurt(source, amount);

        if (isHurt) {
            // При получении урона деактивируем стелс
            deactivateStealth();

            // Если ассасин не был невидим и не на кулдауне, и не в невидимости после Shamak
            if (!wasInvisible && invisibilityCooldown <= 0 && !(isInvisibleAbility && shouldTeleportAttack)) {
                activateInvisibility();
            }

            Entity attacker = source.getEntity();
            if (attacker instanceof LivingEntity livingAttacker && livingAttacker != this) {
                // Устанавливаем атакующего как цель
                this.setTarget(livingAttacker);
            }
        } else {
            // Если урон не прошел (заблокирован), проверяем здоровье для сброса флага Shamak
            float healthPercentage = (this.getHealth() / this.getMaxHealth()) * 100;
            if (healthPercentage > 50) {
                hasUsedShamak = false;
            }
        }

        return isHurt;
    }

    @Override
    public void die(DamageSource source) {
        // Активируем клонирование перед смертью, если есть способность
        if (hasCloneAbility && !this.level().isClientSide) {
            spawnAssassinClones();
        }
        
        super.die(source);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);

        // Случайный выбор оружия при спавне
        int weaponChoice = this.random.nextInt(6); // 0-5 (6 вариантов включая без оружия)

        switch (weaponChoice) {
            case 0:
                // Assassin Dagger
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.ASSASSIN_DAGGER.get()));
                break;
            case 1:
                // Iron Sword
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                break;
            case 2:
                // Iron Axe
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
                break;
            case 3:
                // Iron Hoe
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_HOE));
                break;
            case 4:
                // Iron Hoe
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.KATANA.get()));
                break;
            case 5:
                // Два кинжала ассасина (в обеих руках)
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.ASSASSIN_DAGGER.get()));
                this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.ASSASSIN_DAGGER.get()));
                break;
            case 6:
            default:
                // Без оружия - ничего не выдаём, слоты остаются пустыми
                // Ассасин будет атаковать кулаками с базовым уроном
                break;
        }

        // Инициализируем ману
        this.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            mana.setMaxMana(MAX_MANA);
            mana.setMana(MAX_MANA);
        });

        // Случайно определяем наличие способности Shamak (16% шанс)
        hasShamakAbility = this.random.nextDouble() < 0.16;

        // Случайно определяем наличие способности клонирования (16% шанс)
        hasCloneAbility = this.random.nextDouble() < 0.16;

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
        double chanceCircuit = 0.15;
        double chanceIron = 0.15;

        if (random.nextDouble() < chanceCircuit) {
            int circuitCount = random.nextInt(4, 5);
            this.spawnAtLocation(new ItemStack(ModItems.MAGIC_CIRCUIT.get(), circuitCount));
        }
        if (random.nextDouble() < chanceIron) {
            int circuitCount = random.nextInt(1, 3);
            this.spawnAtLocation(new ItemStack(Items.IRON_INGOT, circuitCount));
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
                    25 // Количество опыта
            ));
        }
    }

    // Кастомная цель для атаки ассасина
    static class AssassinMeleeAttackGoal extends Goal {
        private final AssassinEntity assassin;
        private int attackCooldown = 0;
        private final double speedModifier;
        private boolean attackWithMainHand = true; // Флаг для чередования атак

        public AssassinMeleeAttackGoal(AssassinEntity assassin, double speedModifier) {
            this.assassin = assassin;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = assassin.getTarget();
            return target != null && target.isAlive() && !assassin.hasEffect(MobEffects.INVISIBILITY);
        }

        @Override
        public void tick() {
            LivingEntity target = assassin.getTarget();
            if (target == null) return;

            // Обычная логика атаки (только когда не в невидимости)
            assassin.getNavigation().moveTo(target, speedModifier);
            assassin.getLookControl().setLookAt(target, 30.0F, 30.0F);

            // Проверяем дистанцию для атаки
            double distanceSqr = assassin.distanceToSqr(target);
            if (distanceSqr <= ATTACK_RANGE * ATTACK_RANGE && assassin.hasLineOfSight(target)) {
                if (attackCooldown <= 0) {
                    // Проверяем, есть ли кинжалы в обеих руках или обе руки пустые
                    ItemStack mainStack = assassin.getMainHandItem();
                    ItemStack offStack = assassin.getOffhandItem();
                    boolean hasMainHandDagger = !mainStack.isEmpty() && mainStack.getItem() == ModItems.ASSASSIN_DAGGER.get();
                    boolean hasOffHandDagger = !offStack.isEmpty() && offStack.getItem() == ModItems.ASSASSIN_DAGGER.get();
                    boolean bothEmpty = mainStack.isEmpty() && offStack.isEmpty();
                    boolean canUseBothHands = (hasMainHandDagger && hasOffHandDagger) || bothEmpty;
                    
                    // Выбираем руку для атаки
                    InteractionHand attackHand;
                    if (canUseBothHands) {
                        // Если в обеих руках кинжалы или обе руки пустые — атакуем поочередно
                        attackHand = attackWithMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                        attackWithMainHand = !attackWithMainHand;
                    } else {
                        // Иначе атакуем только основной рукой
                        attackHand = InteractionHand.MAIN_HAND;
                    }
                    
                    // Атакуем всегда - атрибут ATTACK_DAMAGE работает независимо от оружия
                    assassin.swing(attackHand);
                    
                    // Наносим урон
                    float baseDamage = (float) assassin.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    
                    // Бонусный урон в стелсе
                    if (assassin.isStealthed()) {
                        baseDamage *= 1.8f;
                    }
                    
                    target.hurt(assassin.damageSources().mobAttack(assassin), baseDamage);
                    
                    attackCooldown = ATTACK_COOLDOWN_TICKS;
                } else {
                    attackCooldown--;
                }
            } else {
                attackCooldown = 0;
            }
        }
    }

    // Goal для управления поведением во время невидимости
    static class AssassinInvisibilityGoal extends Goal {
        private final AssassinEntity assassin;
        private final double speedModifier;

        public AssassinInvisibilityGoal(AssassinEntity assassin, double speedModifier) {
            this.assassin = assassin;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = assassin.getTarget();
            return target != null && target.isAlive() && assassin.hasEffect(MobEffects.INVISIBILITY);
        }

        @Override
        public void tick() {
            LivingEntity target = assassin.getTarget();
            if (target == null) return;

            // Соблюдаем дистанцию 5 блоков во время невидимости
            double distanceSqr = assassin.distanceToSqr(target);
            double targetDistance = INVISIBILITY_DISTANCE * INVISIBILITY_DISTANCE;

            if (distanceSqr < targetDistance) {
                // Отходим назад, если слишком близко
                Vec3 awayFromTarget = assassin.position().subtract(target.position()).normalize();
                assassin.getNavigation().moveTo(
                        assassin.getX() + awayFromTarget.x * 2,
                        assassin.getY(),
                        assassin.getZ() + awayFromTarget.z * 2,
                        speedModifier
                );
            } else if (distanceSqr > targetDistance * 1.5) {
                // Подходим ближе, если слишком далеко
                assassin.getNavigation().moveTo(target, speedModifier * 0.5);
            }

            // Смотрим на цель
            assassin.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }
    }

}
