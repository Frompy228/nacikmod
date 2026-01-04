package net.artur.nacikmod.entity.MobClass;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
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

        // Разрешаем открывать двери / калитки и немного лучше плавать (как у Villager)
        if (this.getNavigation() instanceof GroundPathNavigation groundNav) {
            groundNav.setCanOpenDoors(true);
        }
        this.getNavigation().setCanFloat(true);
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
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true)); // Открытие дверей (высокий приоритет для боя)
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

    // ===== СИСТЕМА БЛОКИРОВКИ ДЕЙСТВИЙ =====
    
    /**
     * Проверяет, заблокировано ли действие под эффектом SUPPRESSING_GATE
     */
    protected boolean isActionBlocked(ActionType actionType) {
        if (hasEffect(ModEffects.SUPPRESSING_GATE.get())) {
            return actionType.isBlockedBySuppressingGate();
        }
        return false;
    }
    
    /**
     * Перечисление типов действий, которые могут быть заблокированы
     */
    public enum ActionType {
        RANGED_ATTACK(true),    // Дальняя атака (стрельба) - заблокирована
        ABILITY_CAST(true),     // Способности (root, стены, клоны) - заблокированы
        SPECIAL_ATTACK(true),   // Специальные атаки - заблокированы
        MELEE_ATTACK(false),    // Ближний бой - НЕ заблокирован
        MOVEMENT(false);        // Движение - НЕ заблокировано
        
        private final boolean blockedBySuppressingGate;
        
        ActionType(boolean blocked) {
            this.blockedBySuppressingGate = blocked;
        }
        
        public boolean isBlockedBySuppressingGate() {
            return blockedBySuppressingGate;
        }
    }



    // Звуки
    @Override
    protected SoundEvent getAmbientSound() {
        return super.getAmbientSound();
    }


    // ... existing code ...

    @Override
    public boolean startRiding(Entity vehicle) {
        return startRiding(vehicle, false);
    }

    @Override
    public boolean startRiding(Entity vehicle, boolean force) {
        // Блокируем все типы транспорта
        if (vehicle instanceof net.minecraft.world.entity.vehicle.Boat ||           // Лодки
                vehicle instanceof net.minecraft.world.entity.vehicle.AbstractMinecart) { // Все вагонетки
            return false; // Нельзя сесть в транспорт
        }
        return super.startRiding(vehicle, force);
    }

// ... existing code ...

    @Override
    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return super.getHurtSound(pDamageSource);
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return super.getDeathSound();
    }
}