package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class BloodWarriorEntity extends Monster {
    private UUID ownerUUID;
    private Player owner;

    private static final double FOLLOW_DISTANCE = 8.0D;
    private static final int IDLE_DESPAWN_TICKS = 300; // 15 секунд без цели
    private int idleTimer = 0;

    private static final double ATTACK_REACH = 3.0D;
    private int attackCooldown = 0;
    private static final int ATTACK_INTERVAL = 20;

    private boolean effectApplied = false;

    public BloodWarriorEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        if (this.getNavigation() instanceof GroundPathNavigation groundNav) {
            groundNav.setCanOpenDoors(true);
        }
        this.getNavigation().setCanFloat(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(ForgeMod.SWIM_SPEED.get(), 1.5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(2, new BloodWarriorMeleeAttackGoal(this, 1.0D));
        this.goalSelector.addGoal(3, new BloodWarriorFollowOwnerGoal(this, 1.0D));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        // ТАРГЕТЫ
        // 1. Защита самого себя
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, BloodWarriorEntity.class).setAlertOthers(BloodWarriorEntity.class));
        // 2. ЗАЩИТА ХОЗЯИНА: атаковать того, кто ударил хозяина
        this.targetSelector.addGoal(2, new BloodWarriorProtectOwnerGoal(this));
        // 3. МЕСТЬ ХОЗЯИНА: атаковать того, кого ударил хозяин
        this.targetSelector.addGoal(3, new BloodWarriorAttackOwnerTargetGoal(this));
        // 4. Общая агрессия к монстрам
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false,
                this::isValidTarget));
    }

    public boolean isValidTarget(LivingEntity entity) {
        if (entity == null || entity == owner) return false;
        if (entity instanceof BloodWarriorEntity) return false;
        return !(entity instanceof Animal) && !(entity instanceof WaterAnimal);
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
    }

    public Player getOwner() {
        return owner;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Эффект при спавне
            if (!effectApplied) {
                this.addEffect(new MobEffectInstance(ModEffects.BLOOD_EXPLOSION.get(), 2400, 13, false, false));
                effectApplied = true;
            }

            // Восстановление ссылки на владельца после перезагрузки мира
            if (owner == null && ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(ownerUUID);
                if (entity instanceof Player player) {
                    this.owner = player;
                }
            }

            // Логика деспауна при отсутствии задач
            if (this.getTarget() == null || !this.getTarget().isAlive()) {
                idleTimer++;
                if (idleTimer >= IDLE_DESPAWN_TICKS) {
                    this.discard();
                }
            } else {
                idleTimer = 0;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() == owner) return false; // Иммунитет к урону от хозяина
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (owner != null && owner.isAlive()) {
            owner.heal(4.5f);
        }
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity == owner || entity instanceof BloodWarriorEntity) return true;
        return super.isAlliedTo(entity);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (ownerUUID != null) compound.putUUID("OwnerUUID", ownerUUID);
        compound.putBoolean("EffectApplied", effectApplied);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("OwnerUUID")) this.ownerUUID = compound.getUUID("OwnerUUID");
        this.effectApplied = compound.getBoolean("EffectApplied");
    }

    // --- GOALS ---

    static class BloodWarriorMeleeAttackGoal extends Goal {
        private final BloodWarriorEntity warrior;
        private final double speed;

        public BloodWarriorMeleeAttackGoal(BloodWarriorEntity warrior, double speed) {
            this.warrior = warrior;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = warrior.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = warrior.getTarget();
            if (target == null) return;

            warrior.getNavigation().moveTo(target, speed);
            warrior.getLookControl().setLookAt(target, 30.0F, 30.0F);

            if (warrior.distanceToSqr(target) <= ATTACK_REACH * ATTACK_REACH && warrior.hasLineOfSight(target)) {
                if (warrior.attackCooldown <= 0) {
                    warrior.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    warrior.doHurtTarget(target);
                    warrior.attackCooldown = ATTACK_INTERVAL;
                }
            }
            warrior.attackCooldown = Math.max(0, warrior.attackCooldown - 1);
        }
    }

    static class BloodWarriorFollowOwnerGoal extends Goal {
        private final BloodWarriorEntity warrior;
        private final double speed;

        public BloodWarriorFollowOwnerGoal(BloodWarriorEntity warrior, double speed) {
            this.warrior = warrior;
            this.speed = speed;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return warrior.owner != null && warrior.owner.isAlive() &&
                    warrior.getTarget() == null && warrior.distanceToSqr(warrior.owner) > FOLLOW_DISTANCE * FOLLOW_DISTANCE;
        }

        @Override
        public void tick() {
            warrior.getNavigation().moveTo(warrior.owner, speed);
        }
    }

    // Цель: Атаковать того, кто ударил хозяина
    static class BloodWarriorProtectOwnerGoal extends TargetGoal {
        private final BloodWarriorEntity warrior;
        private LivingEntity attacker;
        private int lastHurtTimestamp;

        public BloodWarriorProtectOwnerGoal(BloodWarriorEntity warrior) {
            super(warrior, false);
            this.warrior = warrior;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            Player owner = warrior.getOwner();
            if (owner == null) return false;
            this.attacker = owner.getLastHurtByMob();
            int i = owner.getLastHurtByMobTimestamp();
            return i != this.lastHurtTimestamp && this.canAttack(this.attacker, TargetingConditions.DEFAULT) && warrior.isValidTarget(this.attacker);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.attacker);
            Player owner = warrior.getOwner();
            if (owner != null) this.lastHurtTimestamp = owner.getLastHurtByMobTimestamp();
            super.start();
        }
    }

    // Цель: Атаковать того, кого ударил хозяин
    static class BloodWarriorAttackOwnerTargetGoal extends TargetGoal {
        private final BloodWarriorEntity warrior;
        private LivingEntity target;
        private int lastAttackTimestamp;

        public BloodWarriorAttackOwnerTargetGoal(BloodWarriorEntity warrior) {
            super(warrior, false);
            this.warrior = warrior;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            Player owner = warrior.getOwner();
            if (owner == null) return false;
            this.target = owner.getLastHurtMob();
            int i = owner.getLastHurtMobTimestamp();
            return i != this.lastAttackTimestamp && this.canAttack(this.target, TargetingConditions.DEFAULT) && warrior.isValidTarget(this.target);
        }

        @Override
        public void start() {
            this.mob.setTarget(this.target);
            Player owner = warrior.getOwner();
            if (owner != null) this.lastAttackTimestamp = owner.getLastHurtMobTimestamp();
            super.start();
        }
    }

    @Override
    protected void dropEquipment() {
        // Не дропаем экипировку при смерти
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        // Не дропаем никаких предметов при смерти
    }

    @Override
    protected void dropExperience() {
        // Не дропаем опыт при смерти
    }
}