package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.registry.ModItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

public class MysteriousTraderBattleCloneEntity extends Monster {
    public MysteriousTraderEntity protectedTrader;
    private static final double PROTECTION_RANGE = 8.0D;
    private static final int DESPAWN_TIME = 400; // 20 секунд (20 тиков * 20)
    private int despawnTimer = 0;
    private boolean hasTarget = false;

    public MysteriousTraderBattleCloneEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 15.0)
                .add(Attributes.ARMOR, 5.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(ForgeMod.SWIM_SPEED.get(), 2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.0D, true));
        this.goalSelector.addGoal(2, new FollowTraderGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new ProtectTraderGoal(this));
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);
        
        // Выдаём mana sword
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.MANA_SWORD.get()));
        
        return data;
    }

    @Override
    public void tick() {
        super.tick();
        
        // Ищем торговца для защиты, если ещё не найден
        if (protectedTrader == null || !protectedTrader.isAlive()) {
            findTraderToProtect();
        }
        
        // Проверяем, есть ли цель
        hasTarget = this.getTarget() != null && this.getTarget().isAlive();
        
        // Если нет цели, увеличиваем таймер исчезновения
        if (!hasTarget) {
            despawnTimer++;
            if (despawnTimer >= DESPAWN_TIME) {
                this.discard(); // Исчезаем через 20 секунд без цели
                return;
            }
        } else {
            // Если есть цель, сбрасываем таймер
            despawnTimer = 0;
        }
        
        // Если торговец найден, следуем за ним
        if (protectedTrader != null && protectedTrader.isAlive()) {
            double distance = this.distanceToSqr(protectedTrader);
            if (distance > PROTECTION_RANGE * PROTECTION_RANGE && !hasTarget) {
                // Возвращаемся к торговцу только если нет цели
                this.getNavigation().moveTo(protectedTrader, 1.0D);
            }
        }
    }

    private void findTraderToProtect() {
        AABB searchArea = this.getBoundingBox().inflate(16.0D);
        List<MysteriousTraderEntity> traders = this.level().getEntitiesOfClass(MysteriousTraderEntity.class, searchArea);
        
        if (!traders.isEmpty()) {
            // Выбираем ближайшего торговца
            MysteriousTraderEntity nearestTrader = null;
            double minDistance = Double.MAX_VALUE;
            
            for (MysteriousTraderEntity trader : traders) {
                double distance = this.distanceToSqr(trader);
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestTrader = trader;
                }
            }
            
            if (nearestTrader != null) {
                this.protectedTrader = nearestTrader;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Клоны не получают урон от торговца
        if (source.getEntity() instanceof MysteriousTraderEntity) {
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
        // Клоны не дропают опыт при смерти
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof MysteriousTraderEntity || entity instanceof MysteriousTraderBattleCloneEntity) {
            return true;
        }
        return super.isAlliedTo(entity);
    }

    // Цель для защиты торговца - атакуем только тех, кто атаковал торговца
    static class ProtectTraderGoal extends Goal {
        private final MysteriousTraderBattleCloneEntity clone;
        private int cooldown = 0;

        public ProtectTraderGoal(MysteriousTraderBattleCloneEntity clone) {
            this.clone = clone;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            return clone.protectedTrader != null && 
                   clone.protectedTrader.isAlive() && 
                   cooldown <= 0;
        }

        @Override
        public void tick() {
            if (clone.protectedTrader == null || !clone.protectedTrader.isAlive()) {
                return;
            }

            // Ищем врагов, которые атаковали торговца
            AABB protectionArea = clone.protectedTrader.getBoundingBox().inflate(8.0D);
            List<LivingEntity> enemies = clone.level().getEntitiesOfClass(LivingEntity.class, protectionArea, 
                entity -> {
                    // Проверяем, атаковал ли этот entity торговца
                    if (entity instanceof Player || (entity instanceof Monster && !(entity instanceof MysteriousTraderBattleCloneEntity))) {
                        // Проверяем, есть ли у торговца последний атакующий
                        LivingEntity lastAttacker = clone.protectedTrader.getLastHurtByMob();
                        return lastAttacker == entity && !(entity instanceof MysteriousTraderEntity);
                    }
                    return false;
                });

            if (!enemies.isEmpty()) {
                // Атакуем ближайшего врага, который атаковал торговца
                LivingEntity nearestEnemy = null;
                double minDistance = Double.MAX_VALUE;
                
                for (LivingEntity enemy : enemies) {
                    double distance = clone.distanceToSqr(enemy);
                    if (distance < minDistance) {
                        minDistance = distance;
                        nearestEnemy = enemy;
                    }
                }
                
                if (nearestEnemy != null) {
                    clone.setTarget(nearestEnemy);
                }
            }
            
            cooldown = 20; // Обновляем каждую секунду
        }

        @Override
        public void stop() {
            cooldown = 0;
        }
    }

    // Цель для следования за торговцем
    static class FollowTraderGoal extends Goal {
        private final MysteriousTraderBattleCloneEntity clone;

        public FollowTraderGoal(MysteriousTraderBattleCloneEntity clone) {
            this.clone = clone;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return clone.protectedTrader != null && 
                   clone.protectedTrader.isAlive() && 
                   clone.getTarget() == null; // Только если нет цели
        }

        @Override
        public void tick() {
            if (clone.protectedTrader != null && clone.protectedTrader.isAlive()) {
                double distance = clone.distanceToSqr(clone.protectedTrader);
                if (distance > PROTECTION_RANGE * PROTECTION_RANGE) {
                    clone.getNavigation().moveTo(clone.protectedTrader, 0.8D);
                }
            }
        }
    }


}
