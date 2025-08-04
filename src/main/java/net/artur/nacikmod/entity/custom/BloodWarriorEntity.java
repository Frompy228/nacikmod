package net.artur.nacikmod.entity.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.artur.nacikmod.registry.ModEffects;

public class BloodWarriorEntity extends Monster {
    private UUID ownerUUID;
    private Player owner;
    private static final double PROTECTION_RANGE = 8.0D;
    private static final int DESPAWN_TIME = 300;
    private int despawnTimer = 0;
    private boolean hasTarget = false;
    private static final double ATTACK_RANGE = 3.0D;
    private int attackCooldown = 0;
    private static final int ATTACK_COOLDOWN_TICKS = 20;

    public BloodWarriorEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }
    
    private boolean effectApplied = false;

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
        this.goalSelector.addGoal(1, new BloodWarriorAttackGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new FollowOwnerGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, BloodWarriorEntity.class).setAlertOthers(BloodWarriorEntity.class));
        this.targetSelector.addGoal(2, new ProtectOwnerGoal(this));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, entity -> 
            this.isValidTarget(entity) && this.hasLineOfSight(entity)));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Monster.class, 10, true, false, entity -> 
            this.isValidTarget(entity) && this.hasLineOfSight(entity)));
    }
    
    private boolean isValidTarget(LivingEntity entity) {
        // Атакуем всех враждебных существ, кроме животных, водных животных и других BloodWarrior
        return !(entity instanceof Animal) &&
                !(entity instanceof WaterAnimal) &&
                !(entity instanceof BloodWarriorEntity) &&
                entity != owner; // Не атакуем владельца
    }

    public void setOwner(Player owner) {
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
    }

    public Player getOwner() {
        return owner;
    }
    
    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // Эффект накладываем только один раз
            if (!effectApplied) {
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(ModEffects.BLOOD_EXPLOSION.get(), 2400, 13, false, false));
                effectApplied = true;
            }

            // Ищем владельца
            if (owner == null && ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(ownerUUID);
                if (entity instanceof Player player) {
                    this.owner = player;
                }
            }

            // Проверяем цель
            hasTarget = this.getTarget() != null && this.getTarget().isAlive();

            // Если нет цели — деспаун
            if (!hasTarget) {
                despawnTimer++;
                if (despawnTimer >= DESPAWN_TIME) {
                    this.remove(RemovalReason.DISCARDED); // Работает только на сервере
                    return;
                }
            } else {
                despawnTimer = 0;
            }

            // Навигация к владельцу, если он есть и жив
            if (owner != null && owner.isAlive()) {
                double distance = this.distanceToSqr(owner);
                if (distance > PROTECTION_RANGE * PROTECTION_RANGE && !hasTarget) {
                    this.getNavigation().moveTo(owner, 1.0D);
                }
            }
        }
    }




    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Воины не получают урон от владельца
        if (source.getEntity() == owner) {
            return false;
        }
        
        boolean hurt = super.hurt(source, amount);
        
        if (hurt && source.getEntity() instanceof LivingEntity attacker) {
            // Если нас атаковали, атакуем в ответ
            this.setTarget(attacker);
        }
        
        return hurt;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        // Не дропаем предметы при смерти
    }

    @Override
    protected void dropEquipment() {
        // Не дропаем снаряжение при смерти
    }

    @Override
    protected void dropExperience() {
        // Воины не дропают опыт при смерти
    }
    
    @Override
    public void die(DamageSource source) {
        super.die(source);
        
        // Исцеляем владельца на 4.5 HP при смерти воина
        if (owner != null && owner.isAlive()) {
            owner.heal(4.5f);
        }
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity == owner || entity instanceof BloodWarriorEntity) {
            return true;
        }
        return super.isAlliedTo(entity);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);


        
        return data;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (ownerUUID != null) {
            compound.putUUID("OwnerUUID", ownerUUID);
        }
        compound.putBoolean("EffectApplied", effectApplied);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
        this.effectApplied = compound.getBoolean("EffectApplied");
    }

    // Цель для атаки
    static class BloodWarriorAttackGoal extends Goal {
        private final BloodWarriorEntity warrior;
        private final double speedModifier;

        public BloodWarriorAttackGoal(BloodWarriorEntity warrior, double speedModifier) {
            this.warrior = warrior;
            this.speedModifier = speedModifier;
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

            warrior.getNavigation().moveTo(target, speedModifier);
            warrior.getLookControl().setLookAt(target, 30.0F, 30.0F);

            double distanceSqr = warrior.distanceToSqr(target);
            if (distanceSqr <= ATTACK_RANGE * ATTACK_RANGE && warrior.hasLineOfSight(target)) {
                if (warrior.attackCooldown <= 0) {
                    warrior.swing(net.minecraft.world.InteractionHand.MAIN_HAND);
                    target.hurt(warrior.damageSources().mobAttack(warrior), 
                            (float) warrior.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    warrior.attackCooldown = ATTACK_COOLDOWN_TICKS;
                } else {
                    warrior.attackCooldown--;
                }
            }
        }
    }

    // Цель для защиты владельца - атакуем тех, кто атаковал владельца
    static class ProtectOwnerGoal extends Goal {
        private final BloodWarriorEntity warrior;
        private int cooldown = 0;

        public ProtectOwnerGoal(BloodWarriorEntity warrior) {
            this.warrior = warrior;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            return warrior.owner != null && 
                   warrior.owner.isAlive() && 
                   cooldown <= 0;
        }

        @Override
        public void tick() {
            if (warrior.owner == null || !warrior.owner.isAlive()) {
                return;
            }

            // Ищем всех врагов рядом с владельцем
            AABB protectionArea = warrior.owner.getBoundingBox().inflate(12.0D);
            List<LivingEntity> enemies = warrior.level().getEntitiesOfClass(LivingEntity.class, protectionArea, 
                entity -> {
                    // Атакуем всех враждебных существ рядом с владельцем
                    if (entity instanceof Monster || (entity instanceof Player && entity != warrior.owner)) {
                        return entity != warrior.owner && 
                               warrior.isValidTarget(entity) && 
                               warrior.hasLineOfSight(entity); // Проверяем видимость
                    }
                    return false;
                });

            if (!enemies.isEmpty()) {
                // Атакуем ближайшего врага
                LivingEntity nearestEnemy = null;
                double minDistance = Double.MAX_VALUE;
                
                for (LivingEntity enemy : enemies) {
                    double distance = warrior.distanceToSqr(enemy);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestEnemy = enemy;
                    }
                }
                
                if (nearestEnemy != null) {
                    warrior.setTarget(nearestEnemy);
                }
            }
            
            cooldown = 10; // Обновляем каждые 0.5 секунды для более быстрой реакции
        }

        @Override
        public void stop() {
            cooldown = 0;
        }
    }

    // Цель для следования за владельцем
    static class FollowOwnerGoal extends Goal {
        private final BloodWarriorEntity warrior;

        public FollowOwnerGoal(BloodWarriorEntity warrior) {
            this.warrior = warrior;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return warrior.owner != null && 
                   warrior.owner.isAlive() && 
                   warrior.getTarget() == null; // Только если нет цели
        }

        @Override
        public void tick() {
            if (warrior.owner != null && warrior.owner.isAlive()) {
                double distance = warrior.distanceToSqr(warrior.owner);
                if (distance > PROTECTION_RANGE * PROTECTION_RANGE) {
                    warrior.getNavigation().moveTo(warrior.owner, 0.8D);
                }
            }
        }
    }
} 