package net.artur.nacikmod.entity.MobClass;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;

public class HeroSouls extends Monster {
    private boolean canUseBothHands = false; // Флаг, разрешающий атаки обеими руками
    private int attackCooldown = 20; // Фиксированная перезарядка атаки (20 тиков = 1 сек)
    private double attackRange = 2.0; // Дальность атаки по умолчанию

    // Конструктор
    public HeroSouls(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    // Метод для создания атрибутов (настраивается в классе моба)
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0) // Здоровье
                .add(Attributes.ATTACK_DAMAGE, 6.0) // Урон
                .add(Attributes.MOVEMENT_SPEED, 0.25) // Скорость движения
                .add(Attributes.FOLLOW_RANGE, 32.0); // Радиус обнаружения цели
    }

    // Регистрация целей и поведения
    @Override
    protected void registerGoals() {
        // Базовые цели
        this.goalSelector.addGoal(0, new FloatGoal(this)); // Плавание
        this.goalSelector.addGoal(1, new HeroMeleeAttackGoal(this)); // Кастомная атака
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8)); // Ходьба (избегание воды)
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0f)); // Осмотр игрока
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this)); // Осмотр по сторонам

        // Цели атаки
        this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Player.class, true)); // Приоритет атаки игрока
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, true)); // Атака других мобов
    }

    // Метод для настройки атаки обеими руками
    public void setCanUseBothHands(boolean canUse) {
        this.canUseBothHands = canUse;
    }

    // Метод для проверки, может ли моб атаковать обеими руками
    public boolean canUseBothHands() {
        return this.canUseBothHands;
    }

    // Метод для настройки дальности атаки
    public void setAttackRange(double attackRange) {
        this.attackRange = attackRange;
    }

    // Метод для получения дальности атаки
    public double getAttackRange() {
        return this.attackRange;
    }

    // Внутренний класс для кастомной атаки
    static class HeroMeleeAttackGoal extends Goal {
        private final HeroSouls hero;
        private int attackCooldown = 20;

        public HeroMeleeAttackGoal(HeroSouls mob) {
            this.hero = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return hero.getTarget() != null; // Атака, если есть цель
        }

        @Override
        public void tick() {
            LivingEntity target = hero.getTarget();
            if (target == null) return;

            // Перезарядка атаки
            if (attackCooldown > 0) {
                attackCooldown--;
                return;
            }

            // Проверка дистанции для атаки
            double attackRange = hero.getAttackRange();
            if (hero.distanceToSqr(target) <= attackRange * attackRange) {
                // Атака одной или обеими руками
                if (hero.canUseBothHands()) {
                    hero.swing(InteractionHand.MAIN_HAND);
                    hero.swing(InteractionHand.OFF_HAND);
                    hero.doHurtTarget(target);
                } else {
                    hero.swing(InteractionHand.MAIN_HAND);
                    hero.doHurtTarget(target);
                }
                attackCooldown = 20; // Сброс перезарядки
            }
        }
    }

    // Звуки
    @Override
    protected SoundEvent getAmbientSound() {
        return super.getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return super.getHurtSound(pDamageSource);
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return super.getDeathSound();
    }
}