package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.entity.projectiles.FireBallEntity;
import net.artur.nacikmod.entity.projectiles.IceSpikeProjectile;
import net.artur.nacikmod.entity.projectiles.WeaponProjectile;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
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
import net.artur.nacikmod.util.KnightUtils;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class KnightCasterEntity extends HeroSouls {
    // ================= CONSTANTS =================
    private static final int MAX_MANA = 15000;
    private static final int MANA_REGEN_PER_TICK = 25;
    private static final int BONUS_ARMOR = 15;
    
    // Дистанции
    private static final double OPTIMAL_DISTANCE = 12.0; // Оптимальная дистанция для боя
    private static final double MIN_DISTANCE = 7.0; // Минимальная дистанция (отступаем)
    private static final double MAX_DISTANCE = 25.0; // Максимальная дистанция (приближаемся)
    
    // Кулдауны способностей
    private static final int TELEPORT_COOLDOWN = 200; // 10 секунд
    private static final int INVISIBILITY_COOLDOWN = 240;
    private static final int TELEKINESIS_COOLDOWN = 220; // 12.5 секунд
    private static final int CHAINS_COOLDOWN = 220;
    private static final int PORTAL_SWORDS_COOLDOWN = 120;
    private static final int ICE_SPIKE_COOLDOWN = 280; // 14 секунд
    private static final int GRAVITY_COOLDOWN = 240;
    private static final int FIREBALL_COOLDOWN = 180; // 9 секунд
    
    // Длительности
    private static final int INVISIBILITY_DURATION = 100; // 5 секунд
    private static final int TELEKINESIS_DURATION = 60; // 3 секунды
    private static final int CHAINS_DURATION = 100;
    private static final int CASTING_DURATION = 40; // Длительность анимации каста
    
    // Стоимость маны
    private static final int TELEPORT_MANA_COST = 400;
    private static final int INVISIBILITY_MANA_COST = 300;
    private static final int TELEKINESIS_MANA_COST = 500;
    private static final int CHAINS_MANA_COST = 350;
    private static final int PORTAL_SWORDS_MANA_COST = 600;
    private static final int ICE_SPIKE_MANA_COST = 450;
    private static final int FIREBALL_MANA_COST = 500;
    private static final int GRAVITY_MANA_COST = 400;
    
    // ================= STATE =================
    private int teleportCooldown = 0;
    private int invisibilityCooldown = 0;
    private int telekinesisCooldown = 0;
    private int chainsCooldown = 0;
    private int portalSwordsCooldown = 0;
    private int iceSpikeCooldown = 0;
    private int fireballCooldown = 0;
    private int gravityCooldown = 0;
    
    // Состояние каста для анимаций
    private boolean isCasting = false;
    private int castingTicks = 0;
    
    // Активные способности
    private boolean isInvisible = false;
    private int invisibilityTicks = 0;
    private LivingEntity telekinesisTarget = null;
    private int telekinesisTicks = 0;
    private Vec3 telekinesisStartPos = null;
    private LivingEntity chainsTarget = null;
    private int chainsTicks = 0;
    private List<Entity> activeChains = new ArrayList<>();
    
    // Система иллюзий (как у Illusioner) - для клиентской стороны
    private int clientSideIllusionTicks = 0;
    private final Vec3[][] clientSideIllusionOffsets = new Vec3[2][4]; // [0] = предыдущие, [1] = текущие
    
    // Синхронизация данных
    private static final EntityDataAccessor<Boolean> DATA_CASTING = SynchedEntityData.defineId(KnightCasterEntity.class, EntityDataSerializers.BOOLEAN);
    
    public KnightCasterEntity(EntityType<? extends HeroSouls> entityType, Level level) {
        super(entityType, level);
        this.setCanUseBothHands(true);
        
        // Инициализация смещений иллюзий
        for (int i = 0; i < 4; ++i) {
            this.clientSideIllusionOffsets[0][i] = Vec3.ZERO;
            this.clientSideIllusionOffsets[1][i] = Vec3.ZERO;
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(ModAttributes.BONUS_ARMOR.get(), BONUS_ARMOR)
                .add(Attributes.MAX_HEALTH, 70.0)
                .add(Attributes.ARMOR, 10.0)
                .add(Attributes.ARMOR_TOUGHNESS, 8.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(ForgeMod.SWIM_SPEED.get(), 2)
                .add(Attributes.FOLLOW_RANGE, 40.0);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CASTING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(2, new CasterCombatGoal(this));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(KnightCasterEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, this::isValidTarget));
    }

    private boolean isValidTarget(LivingEntity entity) {
        return !(entity instanceof Animal) &&
                !(entity instanceof WaterAnimal) &&
                !(entity instanceof KnightEntity) &&
                !(entity instanceof KnightArcherEntity) &&
                !(entity instanceof KnightPaladinEntity) &&
                !(entity instanceof KnightBossEntity) &&
                !(entity instanceof KnightCasterEntity) &&
                !(entity instanceof KnightLeaderEntity);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide) {
            // Регенерация маны через capability
            if (this.tickCount % 5 == 0) {
                this.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                    if (mana.getMana() < mana.getMaxMana()) {
                        mana.addMana(MANA_REGEN_PER_TICK);
                    }
                });
            }
            
            // Обновление кулдаунов
            if (teleportCooldown > 0) teleportCooldown--;
            if (invisibilityCooldown > 0) invisibilityCooldown--;
            if (telekinesisCooldown > 0) telekinesisCooldown--;
            if (chainsCooldown > 0) chainsCooldown--;
            if (portalSwordsCooldown > 0) portalSwordsCooldown--;
            if (iceSpikeCooldown > 0) iceSpikeCooldown--;
            if (fireballCooldown > 0) fireballCooldown--;
            if (gravityCooldown > 0) gravityCooldown--;
            
            // Обновление состояния каста
            if (isCasting) {
                castingTicks--;
                if (castingTicks <= 0) {
                    isCasting = false;
                    this.entityData.set(DATA_CASTING, false);
                }
            }
            
            // Обновление активных способностей
            tickInvisibility();
            tickTelekinesis();
            tickChains();
            tickPendingPortalSwords();
            tickPendingFallingSword();
            tickPendingIceSpike();
            tickPendingFireball();
        }
    }
    
    @Override
    public void aiStep() {
        super.aiStep();
        
        // Обновление смещений иллюзий на клиенте (как у Illusioner)
        if (this.level().isClientSide && this.isInvisible()) {
            --this.clientSideIllusionTicks;
            if (this.clientSideIllusionTicks < 0) {
                this.clientSideIllusionTicks = 0;
            }
            
            // Обновляем смещения при получении урона или каждые 60 секунд
            if (this.hurtTime != 1 && this.tickCount % 1200 != 0) {
                if (this.hurtTime == this.hurtDuration - 1) {
                    this.clientSideIllusionTicks = 3;
                    
                    for (int k = 0; k < 4; ++k) {
                        this.clientSideIllusionOffsets[0][k] = this.clientSideIllusionOffsets[1][k];
                        this.clientSideIllusionOffsets[1][k] = Vec3.ZERO;
                    }
                }
            } else {
                this.clientSideIllusionTicks = 3;
                float f = -6.0F;
                int i = 13;
                
                for (int j = 0; j < 4; ++j) {
                    this.clientSideIllusionOffsets[0][j] = this.clientSideIllusionOffsets[1][j];
                    this.clientSideIllusionOffsets[1][j] = new Vec3(
                        (double)(-6.0F + (float)this.random.nextInt(13)) * 0.5D,
                        (double)Math.max(0, this.random.nextInt(6) - 4),
                        (double)(-6.0F + (float)this.random.nextInt(13)) * 0.5D
                    );
                }
                
                // Частицы при создании иллюзий
                for (int l = 0; l < 16; ++l) {
                    this.level().addParticle(ParticleTypes.CLOUD, this.getRandomX(0.5D), this.getRandomY(), this.getZ(0.5D), 0.0D, 0.0D, 0.0D);
                }
                
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ILLUSIONER_MIRROR_MOVE, this.getSoundSource(), 1.0F, 1.0F, false);
            }
        }
    }
    
    /**
     * Получает смещения для иллюзий (как у Illusioner).
     * Используется в рендерере для отрисовки нескольких копий модели.
     */
    public Vec3[] getIllusionOffsets(float partialTicks) {
        if (this.clientSideIllusionTicks <= 0) {
            return this.clientSideIllusionOffsets[1];
        } else {
            double d0 = (double)(((float)this.clientSideIllusionTicks - partialTicks) / 3.0F);
            d0 = Math.pow(d0, 0.25D);
            Vec3[] avec3 = new Vec3[4];
            
            for (int i = 0; i < 4; ++i) {
                avec3[i] = this.clientSideIllusionOffsets[1][i].scale(1.0D - d0).add(this.clientSideIllusionOffsets[0][i].scale(d0));
            }
            
            return avec3;
        }
    }
    
    private void tickPendingFallingSword() {
        if (!this.getPersistentData().contains("pendingFallingSword")) return;
        
        CompoundTag swordData = this.getPersistentData().getCompound("pendingFallingSword");
        int spawnTick = swordData.getInt("spawnTick");
        
        if (this.tickCount - spawnTick >= 20) { // 1 секунда задержки - мечи падают ПЕРЕД ice spike
            int targetId = swordData.getInt("targetId");
            Entity targetEntity = this.level().getEntity(targetId);
            
            if (targetEntity instanceof LivingEntity target && target.isAlive() && this.isAlive()) {
                spawnFallingSword(target);
            }
            
            this.getPersistentData().remove("pendingFallingSword");
        }
    }
    
    private void tickPendingIceSpike() {
        if (!this.getPersistentData().contains("pendingIceSpike")) return;
        
        CompoundTag spikeData = this.getPersistentData().getCompound("pendingIceSpike");
        int spawnTick = spikeData.getInt("spawnTick");
        
        if (this.tickCount - spawnTick >= 40) { // 2 секунды задержки - ice spike после мечей
            int targetId = spikeData.getInt("targetId");
            Entity targetEntity = this.level().getEntity(targetId);
            
            if (targetEntity instanceof LivingEntity target && target.isAlive() && this.isAlive()) {
                IceSpikeProjectile spike = new IceSpikeProjectile(this.level(), this, 10.0F);
                Vec3 shootPos = this.getEyePosition();
                Vec3 targetPos = target.getEyePosition();
                Vec3 direction = targetPos.subtract(shootPos).normalize();
                
                spike.setPos(shootPos.x, shootPos.y, shootPos.z);
                spike.shoot(direction.x, direction.y, direction.z, 1.5F, 0.1F);
                this.level().addFreshEntity(spike);
            }
            
            this.getPersistentData().remove("pendingIceSpike");
        }
    }
    
    // ================= УМНЫЙ ИИ =================
    
    static class CasterCombatGoal extends Goal {
        private final KnightCasterEntity caster;
        private int abilityDecisionTicks = 0;
        private static final int DECISION_INTERVAL = 20; // Решение каждую секунду
        
        public CasterCombatGoal(KnightCasterEntity caster) {
            this.caster = caster;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }
        
        @Override
        public boolean canUse() {
            return caster.getTarget() != null && caster.getTarget().isAlive();
        }
        
        @Override
        public void tick() {
            LivingEntity target = caster.getTarget();
            if (target == null || !target.isAlive()) return;
            
            double distance = caster.distanceTo(target);
            caster.getLookControl().setLookAt(target, 30.0F, 30.0F);
            
            // Умное движение - держим дистанцию
            if (distance < MIN_DISTANCE) {
                // Отступаем
                Vec3 away = caster.position().subtract(target.position()).normalize();
                caster.getNavigation().moveTo(
                    caster.getX() + away.x * 3,
                    caster.getY(),
                    caster.getZ() + away.z * 3,
                    1.2
                );
            } else if (distance > MAX_DISTANCE) {
                // Приближаемся
                caster.getNavigation().moveTo(target, 1.0);
            } else if (distance > OPTIMAL_DISTANCE + 2) {
                // Круговое движение для оптимальной дистанции
                double angle = (caster.tickCount % 40 < 20) ? Math.PI / 2 : -Math.PI / 2;
                Vec3 dir = target.position().subtract(caster.position()).normalize();
                double x = -dir.z * Math.sin(angle);
                double z = dir.x * Math.sin(angle);
                caster.getNavigation().moveTo(
                    caster.getX() + x,
                    caster.getY(),
                    caster.getZ() + z,
                    1.0
                );
            } else {
                caster.getNavigation().stop();
            }
            
            // Умный выбор способностей
            abilityDecisionTicks++;
            if (abilityDecisionTicks >= DECISION_INTERVAL) {
                abilityDecisionTicks = 0;
                caster.decideAndUseAbility(target, distance);
            }
        }
    }
    
    // ================= СИСТЕМА ПРИНЯТИЯ РЕШЕНИЙ =================
    
    private void decideAndUseAbility(LivingEntity target, double distance) {
        if (isActionBlocked(ActionType.ABILITY_CAST)) return;
        
        // Приоритет 1: Если враг близко и нет возможности телепортироваться - используем цепи
        if (distance < MIN_DISTANCE && teleportCooldown > 0 && chainsCooldown <= 0 && hasMana(CHAINS_MANA_COST)) {
            useChains(target);
            return;
        }
        
        // Приоритет 2: Если враг близко - телепортируемся за спину
        if (distance < MIN_DISTANCE && teleportCooldown <= 0 && hasMana(TELEPORT_MANA_COST)) {
            useTeleport(target);
            return;
        }
        
        // Приоритет 3: Если цель в телекинезе - стреляем мечом
        if (telekinesisTarget == target && portalSwordsCooldown <= 0 && hasMana(PORTAL_SWORDS_MANA_COST)) {
            usePortalSwords(target);
            return;
        }
        
        // Приоритет 4: Если цель связана цепями - используем телекинез или мечи
        if (chainsTarget == target) {
            if (telekinesisCooldown <= 0 && hasMana(TELEKINESIS_MANA_COST)) {
                useTelekinesis(target);
                return;
            } else if (portalSwordsCooldown <= 0 && hasMana(PORTAL_SWORDS_MANA_COST)) {
                usePortalSwords(target);
                return;
            }
        }
        
        // Приоритет 5: Если цель далеко - сначала цепи, потом мечи
        if (distance > OPTIMAL_DISTANCE) {
            if (chainsCooldown <= 0 && hasMana(CHAINS_MANA_COST)) {
                useChains(target);
                return;
            } else if (portalSwordsCooldown <= 0 && hasMana(PORTAL_SWORDS_MANA_COST)) {
                usePortalSwords(target);
                return;
            }
        }
        
        // Приоритет 6: Ледяные шипы + падающий меч (комбо)
        if (iceSpikeCooldown <= 0 && hasMana(ICE_SPIKE_MANA_COST)) {
            useIceSpikeCombo(target);
            return;
        }
        
        // Приоритет 7: Невидимость для защиты
        if (getHealth() < getMaxHealth() * 0.4f && invisibilityCooldown <= 0 && hasMana(INVISIBILITY_MANA_COST)) {
            useInvisibility();
            return;
        }
        
        // Приоритет 8: FireBall с неба
        if (fireballCooldown <= 0 && hasMana(FIREBALL_MANA_COST) && distance > OPTIMAL_DISTANCE) {
            useFireball(target);
            return;
        }
        
        // Приоритет 9: Усиленная гравитация
        if (gravityCooldown <= 0 && hasMana(GRAVITY_MANA_COST) && distance < OPTIMAL_DISTANCE) {
            useGravity(target);
            return;
        }
    }
    
    // ================= СПОСОБНОСТИ =================
    
    private void useTeleport(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        if (!consumeMana(TELEPORT_MANA_COST)) return;
        startCasting();
        
        // Телепортируемся за спину цели на расстоянии равном дистанции до цели
        double distance = this.distanceTo(target);
        Vec3 targetLookDirection = target.getLookAngle().normalize();
        Vec3 teleportDirection = targetLookDirection.scale(-distance); // За спину на том же расстоянии
        
        Vec3 targetPos = target.position();
        Vec3 teleportPos = targetPos.add(teleportDirection);
        
        // Находим правильную высоту
        double spawnY = target.getY();
        BlockPos checkPos = BlockPos.containing(teleportPos.x, spawnY, teleportPos.z);
        if (!this.level().isEmptyBlock(checkPos)) {
            spawnY = target.getY() + 1.0D;
        }
        
        this.setPos(teleportPos.x, spawnY, teleportPos.z);
        this.hurtMarked = true;
        this.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES, target.getEyePosition());
        
        // Эффекты
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                serverLevel.sendParticles(ParticleTypes.PORTAL,
                    this.getX(), this.getY(), this.getZ(),
                    1, 0.5, 0.5, 0.5, 0.1);
            }
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1.0F, 1.0F);
        
        teleportCooldown = TELEPORT_COOLDOWN;
    }
    
    private void useInvisibility() {
        if (!consumeMana(INVISIBILITY_MANA_COST)) return;
        startCasting();
        
        this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, INVISIBILITY_DURATION, 0, false, false));
        isInvisible = true;
        invisibilityTicks = INVISIBILITY_DURATION;
        invisibilityCooldown = INVISIBILITY_COOLDOWN;
        
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.HOSTILE, 1.0F, 1.0F);
    }
    
    private void tickInvisibility() {
        if (isInvisible) {
            invisibilityTicks--;
            if (invisibilityTicks <= 0) {
                isInvisible = false;
            }
        }
    }
    
    private void useTelekinesis(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        if (!consumeMana(TELEKINESIS_MANA_COST)) return;
        startCasting();
        
        // Проверяем, есть ли активные цепи на цели - разрываем их с эффектами
        boolean chainsBroken = breakChainsOnTarget(target);
        if (chainsBroken) {
            // Дополнительный урон при разрыве цепей
            target.hurt(this.damageSources().mobAttack(this), 15.0F);
        }
        
        telekinesisTarget = target;
        telekinesisTicks = TELEKINESIS_DURATION;
        telekinesisStartPos = target.position();
        telekinesisCooldown = TELEKINESIS_COOLDOWN;
        
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.HOSTILE, 1.0F, 1.0F);
    }
    
    /**
     * Разрывает цепи на цели с красивыми визуальными эффектами.
     * @return true если цепи были разорваны, false если их не было
     */
    private boolean breakChainsOnTarget(LivingEntity target) {
        boolean foundChains = false;
        List<Entity> chainsToRemove = new ArrayList<>();
        
        // Ищем все активные цепи, которые держат эту цель
        for (Entity chain : activeChains) {
            if (chain instanceof ChainEntity chainEntity && chainEntity.isAlive()) {
                // Проверяем, держит ли эта цепь нашу цель
                if (chainEntity.getTargetId() == target.getId()) {
                    chainsToRemove.add(chain);
                    foundChains = true;
                }
            }
        }
        
        if (foundChains && this.level() instanceof ServerLevel serverLevel) {
            // Удаляем цепи
            for (Entity chain : chainsToRemove) {
                chain.discard();
            }
            activeChains.removeAll(chainsToRemove);
            
            // Если это была наша цель в chainsTarget, сбрасываем
            if (chainsTarget == target) {
                chainsTarget = null;
                chainsTicks = 0;
            }
            
            // Красивые визуальные эффекты разрыва цепей
            Vec3 targetPos = target.position();
            double centerY = targetPos.y + target.getBbHeight() / 2;
            
            // Взрыв частиц цепей - разлетаются во все стороны
            for (int i = 0; i < 120; i++) {
                double angle = (2.0 * Math.PI * i) / 120.0;
                double radius = 1.5 + this.random.nextDouble() * 2.0;
                double height = (this.random.nextDouble() - 0.5) * target.getBbHeight();
                double x = targetPos.x + Math.cos(angle) * radius;
                double y = centerY + height;
                double z = targetPos.z + Math.sin(angle) * radius;
                
                // Смешиваем разные типы частиц для красивого эффекта
                SimpleParticleType particleType;
                double speed = 0.15 + this.random.nextDouble() * 0.1;
                if (i % 4 == 0) {
                    particleType = ParticleTypes.CRIT;
                } else if (i % 4 == 1) {
                    particleType = ParticleTypes.ENCHANT;
                } else if (i % 4 == 2) {
                    particleType = ParticleTypes.PORTAL;
                } else {
                    particleType = ParticleTypes.ASH;
                }
                
                serverLevel.sendParticles(particleType,
                    x, y, z,
                    1, 0.3, 0.3, 0.3, speed);
            }
            
            // Взрывные частицы в центре (эффект разрыва)
            for (int i = 0; i < 30; i++) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                    targetPos.x + (this.random.nextDouble() - 0.5) * 0.5,
                    centerY + (this.random.nextDouble() - 0.5) * 0.5,
                    targetPos.z + (this.random.nextDouble() - 0.5) * 0.5,
                    1, 0.3, 0.3, 0.3, 0.05);
            }
            
            // Дополнительные частицы магии (синие/фиолетовые)
            for (int i = 0; i < 40; i++) {
                double angle = (2.0 * Math.PI * i) / 40.0;
                double radius = 1.0 + this.random.nextDouble() * 1.5;
                double x = targetPos.x + Math.cos(angle) * radius;
                double y = centerY + (this.random.nextDouble() - 0.5) * 1.0;
                double z = targetPos.z + Math.sin(angle) * radius;
                
                serverLevel.sendParticles(ParticleTypes.WITCH,
                    x, y, z,
                    1, 0.2, 0.2, 0.2, 0.08);
            }
            
            // Звуки разрыва цепей (используем доступные звуки)
            serverLevel.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                SoundEvents.ANVIL_BREAK, SoundSource.HOSTILE, 1.5F, 0.8F);
            serverLevel.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                SoundEvents.IRON_DOOR_CLOSE, SoundSource.HOSTILE, 1.0F, 1.2F);
            serverLevel.playSound(null, targetPos.x, targetPos.y, targetPos.z,
                SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.8F, 1.5F);
        }
        
        return foundChains;
    }
    
    private void tickTelekinesis() {
        if (telekinesisTarget == null || telekinesisTicks <= 0) {
            if (telekinesisTarget != null && telekinesisTarget.isAlive()) {
                // Резко бросаем вниз
                Vec3 downForce = new Vec3(0, -2.5, 0);
                telekinesisTarget.setDeltaMovement(telekinesisTarget.getDeltaMovement().add(downForce));
                telekinesisTarget.hurtMarked = true;
                telekinesisTarget.hurt(this.damageSources().mobAttack(this), 20.0F);
                
                // Эффекты падения
                if (this.level() instanceof ServerLevel serverLevel) {
                    for (int i = 0; i < 50; i++) {
                        serverLevel.sendParticles(ParticleTypes.CLOUD,
                            telekinesisTarget.getX(), telekinesisTarget.getY(), telekinesisTarget.getZ(),
                            1, 0.5, 0.5, 0.5, 0.15);
                    }
                    serverLevel.playSound(null, telekinesisTarget.getX(), telekinesisTarget.getY(), telekinesisTarget.getZ(),
                        SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.5F, 0.8F);
                }
            }
            telekinesisTarget = null;
            telekinesisTicks = 0;
            telekinesisStartPos = null;
            return;
        }
        
        if (telekinesisTarget.isAlive()) {
            // Автоматически разрываем цепи, если цель все еще в цепях
            if (breakChainsOnTarget(telekinesisTarget)) {
                // Дополнительный урон при разрыве цепей во время телекинеза
                telekinesisTarget.hurt(this.damageSources().mobAttack(this), 10.0F);
            }
            
            // Поднимаем в воздух с плавным движением
            Vec3 targetPos = telekinesisTarget.position();
            double height = 3.0 + Math.sin(this.tickCount * 0.1) * 0.5;
            Vec3 desiredPos = telekinesisStartPos.add(0, height, 0);
            Vec3 force = desiredPos.subtract(targetPos).scale(0.2);
            
            // Ограничиваем силу для плавности
            force = new Vec3(
                Mth.clamp(force.x, -0.3, 0.3),
                Mth.clamp(force.y, -0.1, 0.4),
                Mth.clamp(force.z, -0.3, 0.3)
            );
            
            telekinesisTarget.setDeltaMovement(telekinesisTarget.getDeltaMovement().add(force));
            telekinesisTarget.hurtMarked = true;
            telekinesisTarget.resetFallDistance();
            
            // Эффекты телекинеза
            if (this.level() instanceof ServerLevel serverLevel) {
                if (this.tickCount % 3 == 0) {
                    serverLevel.sendParticles(ParticleTypes.ENCHANT,
                        telekinesisTarget.getX(), telekinesisTarget.getY(), telekinesisTarget.getZ(),
                        5, 0.3, 0.3, 0.3, 0.05);
                }
                if (this.tickCount % 10 == 0) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                        telekinesisTarget.getX(), telekinesisTarget.getY(), telekinesisTarget.getZ(),
                        3, 0.2, 0.2, 0.2, 0.1);
                }
            }
        } else {
            telekinesisTarget = null;
            telekinesisTicks = 0;
            telekinesisStartPos = null;
            return;
        }
        
        telekinesisTicks--;
    }
    
    private void useChains(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        if (!consumeMana(CHAINS_MANA_COST)) return;
        startCasting();
        
        chainsTarget = target;
        chainsTicks = CHAINS_DURATION;
        chainsCooldown = CHAINS_COOLDOWN;
        
        // Реальные цепи: отдельная сущность, которая жестко фиксирует цель
        if (this.level() instanceof ServerLevel serverLevel) {
            ChainEntity chain = new ChainEntity(serverLevel, ModEntities.CHAIN_ENTITY.get(), target, CHAINS_DURATION);
            serverLevel.addFreshEntity(chain);
            activeChains.add(chain);
        }
        
        // Визуальные эффекты цепей
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 20; i++) {
                double angle = (2.0 * Math.PI * i) / 20.0;
                double radius = 1.5;
                double x = target.getX() + Math.cos(angle) * radius;
                double y = target.getY() + this.random.nextDouble() * target.getBbHeight();
                double z = target.getZ() + Math.sin(angle) * radius;
                
                serverLevel.sendParticles(ParticleTypes.CRIT,
                    x, y, z,
                    1, 0.1, 0.1, 0.1, 0.05);
            }
        }
        
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.CHAIN_PLACE, SoundSource.HOSTILE, 1.0F, 0.8F);
    }
    
    private void tickChains() {
        if (chainsTarget == null || chainsTicks <= 0) {
            chainsTarget = null;
            chainsTicks = 0;
            activeChains.removeIf(e -> e.isRemoved() || !e.isAlive());
            return;
        }
        
        if (!chainsTarget.isAlive()) {
            chainsTarget = null;
            chainsTicks = 0;
            activeChains.removeIf(e -> e.isRemoved() || !e.isAlive());
            return;
        }
        
        chainsTicks--;
    }
    
    private void usePortalSwords(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        if (!consumeMana(PORTAL_SWORDS_MANA_COST)) return;
        startCasting();
        
        portalSwordsCooldown = PORTAL_SWORDS_COOLDOWN;
        
        // Создаем порталы вокруг цели и мечи из них
        Vec3 targetPos = target.position();
        int swordCount = 6;
        double radius = 4.0;
        
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < swordCount; i++) {
                double angle = (2.0 * Math.PI * i) / swordCount;
                double x = targetPos.x + Math.cos(angle) * radius;
                double y = targetPos.y + 2.0;
                double z = targetPos.z + Math.sin(angle) * radius;
                
                // Создаем портал (частицы)
                for (int j = 0; j < 50; j++) {
                    serverLevel.sendParticles(ParticleTypes.PORTAL,
                        x, y, z,
                        1, 0.5, 0.5, 0.5, 0.1);
                }
                
                // Сохраняем данные для создания меча через 40 тиков (2 секунды)
                CompoundTag swordData = new CompoundTag();
                swordData.putDouble("x", x);
                swordData.putDouble("y", y);
                swordData.putDouble("z", z);
                swordData.putInt("targetId", target.getId());
                swordData.putInt("spawnTick", this.tickCount);
                this.getPersistentData().put("pendingPortalSword_" + i, swordData);
            }
        }
        
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ENDERMAN_SCREAM, SoundSource.HOSTILE, 1.0F, 0.8F);
    }
    
    private void tickPendingPortalSwords() {
        // Собираем ключи для удаления отдельно, чтобы не менять Map во время итерации (ConcurrentModificationException)
        List<String> toRemove = new ArrayList<>();
        for (String key : this.getPersistentData().getAllKeys()) {
            if (!key.startsWith("pendingPortalSword_")) continue;
            CompoundTag swordData = this.getPersistentData().getCompound(key);
            int spawnTick = swordData.getInt("spawnTick");
            if (this.tickCount - spawnTick < 40) continue;

            double x = swordData.getDouble("x");
            double y = swordData.getDouble("y");
            double z = swordData.getDouble("z");
            int targetId = swordData.getInt("targetId");
            Entity targetEntity = this.level().getEntity(targetId);
            if (targetEntity instanceof LivingEntity target && target.isAlive() && this.isAlive()) {
                WeaponProjectile sword = new WeaponProjectile(ModEntities.WEAPON_PROJECTILE.get(), this.level());
                sword.setStack(new ItemStack(Items.IRON_SWORD));
                sword.setPos(x, y, z);
                Vec3 direction = target.position().subtract(x, y, z).normalize();
                sword.shoot(direction.x, direction.y, direction.z, 2.5F, 0.1F);
                this.level().addFreshEntity(sword);
            }
            toRemove.add(key);
        }
        for (String key : toRemove) {
            this.getPersistentData().remove(key);
        }
    }
    
    private void useIceSpikeCombo(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        if (!consumeMana(ICE_SPIKE_MANA_COST)) return;
        startCasting();
        
        iceSpikeCooldown = ICE_SPIKE_COOLDOWN;
        
        // Сначала падают мечи (через 20 тиков), потом ice spike (через 40 тиков)
        // Это предотвращает попадание мечей в ледяную тюрьму
        CompoundTag swordData = new CompoundTag();
        swordData.putInt("targetId", target.getId());
        swordData.putInt("spawnTick", this.tickCount);
        this.getPersistentData().put("pendingFallingSword", swordData);
        
        // Ice spike запускаем через 40 тиков (после падения мечей)
        CompoundTag spikeData = new CompoundTag();
        spikeData.putInt("targetId", target.getId());
        spikeData.putInt("spawnTick", this.tickCount);
        this.getPersistentData().put("pendingIceSpike", spikeData);
        
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.GLASS_BREAK, SoundSource.HOSTILE, 1.0F, 0.8F);
    }
    
    private void spawnFallingSword(LivingEntity target) {
        // TODO: Создать FallingSwordEntity с большим хитбоксом и уроном сквозь блоки
        Vec3 targetPos = target.position();
        double spawnY = targetPos.y + 15.0;
        
        if (this.level() instanceof ServerLevel serverLevel) {
            // Создаем несколько мечей для большого хитбокса
            for (int i = 0; i < 5; i++) {
                double offsetX = (this.random.nextDouble() - 0.5) * 2.0;
                double offsetZ = (this.random.nextDouble() - 0.5) * 2.0;
                
                WeaponProjectile sword = new WeaponProjectile(ModEntities.WEAPON_PROJECTILE.get(), serverLevel);
                sword.setStack(new ItemStack(Items.DIAMOND_SWORD));
                sword.setPos(targetPos.x + offsetX, spawnY, targetPos.z + offsetZ);
                sword.shoot(0, -1, 0, 3.0F, 0.0F);
                serverLevel.addFreshEntity(sword);
            }
            
            // Эффекты падения
            for (int i = 0; i < 100; i++) {
                serverLevel.sendParticles(ParticleTypes.CLOUD,
                    targetPos.x, spawnY, targetPos.z,
                    1, 2.0, 0.1, 2.0, 0.1);
            }
            
            this.level().playSound(null, targetPos.x, targetPos.y, targetPos.z,
                SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 2.0F, 0.8F);
        }
    }
    
    private void useFireball(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        if (!consumeMana(FIREBALL_MANA_COST)) return;
        
        fireballCooldown = FIREBALL_COOLDOWN;
        startCasting();
        
        // FireBall падает с неба через 30 тиков (1.5 секунды)
        CompoundTag fireballData = new CompoundTag();
        fireballData.putInt("targetId", target.getId());
        fireballData.putInt("spawnTick", this.tickCount);
        this.getPersistentData().put("pendingFireball", fireballData);
        
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.EVOKER_PREPARE_ATTACK, SoundSource.HOSTILE, 1.0F, 1.0F);
    }
    
    private void tickPendingFireball() {
        if (!this.getPersistentData().contains("pendingFireball")) return;
        
        CompoundTag fireballData = this.getPersistentData().getCompound("pendingFireball");
        int spawnTick = fireballData.getInt("spawnTick");
        
        if (this.tickCount - spawnTick >= 30) { // 1.5 секунды задержки
            int targetId = fireballData.getInt("targetId");
            Entity targetEntity = this.level().getEntity(targetId);
            
            if (targetEntity instanceof LivingEntity target && target.isAlive() && this.isAlive()) {
                Vec3 targetPos = target.position();
                double spawnY = targetPos.y + 20.0; // Высота падения
                
                if (this.level() instanceof ServerLevel serverLevel) {
                    FireBallEntity fireball = new FireBallEntity(serverLevel, this);
                    fireball.setPos(targetPos.x, spawnY, targetPos.z);
                    // Падает вниз
                    fireball.shoot(new Vec3(0, -1, 0), 1.5F);
                    serverLevel.addFreshEntity(fireball);
                    
                    // Эффекты падения
                    for (int i = 0; i < 50; i++) {
                        serverLevel.sendParticles(ParticleTypes.FLAME,
                            targetPos.x, spawnY, targetPos.z,
                            1, 2.0, 0.1, 2.0, 0.1);
                    }
                }
            }
            
            this.getPersistentData().remove("pendingFireball");
        }
    }
    
    private void useGravity(LivingEntity target) {
        if (target == null || !target.isAlive()) return;
        if (!consumeMana(GRAVITY_MANA_COST)) return;
        startCasting();
        
        gravityCooldown = GRAVITY_COOLDOWN;
        
        // Усиленная гравитация вокруг цели (ваш эффект EffectEnhancedGravity)
        // NB: new AABB(Vec3) не существует, поэтому берем bounding box цели.
        AABB area = target.getBoundingBox().inflate(5.0D);
        for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity == this) continue;
            if (!entity.isAlive()) continue;
            if (!this.isValidTarget(entity)) continue;

            entity.addEffect(new MobEffectInstance(ModEffects.ENHANCED_GRAVITY.get(), 100, 0, false, true));
        }

        // Частицы вокруг цели
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 25; i++) {
                double x = target.getX() + (this.random.nextDouble() - 0.5D) * 6.0D;
                double y = target.getY() + this.random.nextDouble() * target.getBbHeight();
                double z = target.getZ() + (this.random.nextDouble() - 0.5D) * 6.0D;
                serverLevel.sendParticles(ParticleTypes.ASH, x, y, z, 1, 0.0D, 0.02D, 0.0D, 0.0D);
            }
        }
        
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.0F, 0.8F);
    }
    
    // ================= ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ =================
    
    private boolean hasMana(int amount) {
        return this.getCapability(ManaProvider.MANA_CAPABILITY)
            .map(mana -> mana.getMana() >= amount)
            .orElse(false);
    }
    
    private boolean consumeMana(int amount) {
        return this.getCapability(ManaProvider.MANA_CAPABILITY)
            .map(mana -> {
                if (mana.getMana() >= amount) {
                    mana.removeMana(amount);
                    return true;
                }
                return false;
            })
            .orElse(false);
    }
    
    public int getMana() {
        return this.getCapability(ManaProvider.MANA_CAPABILITY)
            .map(mana -> mana.getMana())
            .orElse(0);
    }
    
    // Методы для анимаций каста
    private void startCasting() {
        isCasting = true;
        castingTicks = CASTING_DURATION;
        this.entityData.set(DATA_CASTING, true);
    }
    
    public boolean isCasting() {
        return this.entityData.get(DATA_CASTING);
    }
    
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, tag);
        
        this.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            mana.setMaxMana(MAX_MANA);
            mana.setMana(MAX_MANA);
        });
        
        this.getAttribute(ModAttributes.BONUS_ARMOR.get()).setBaseValue(BONUS_ARMOR);
        return data;
    }
    
    @Override
    protected void dropEquipment() {}
    
    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {}
    
    @Override
    protected void dropExperience() {}
    
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TeleportCooldown", teleportCooldown);
        tag.putInt("InvisibilityCooldown", invisibilityCooldown);
        tag.putInt("TelekinesisCooldown", telekinesisCooldown);
        tag.putInt("ChainsCooldown", chainsCooldown);
        tag.putInt("PortalSwordsCooldown", portalSwordsCooldown);
        tag.putInt("IceSpikeCooldown", iceSpikeCooldown);
        tag.putInt("FireballCooldown", fireballCooldown);
        tag.putInt("GravityCooldown", gravityCooldown);
        tag.putBoolean("IsInvisible", isInvisible);
        tag.putInt("InvisibilityTicks", invisibilityTicks);
        tag.putInt("TelekinesisTicks", telekinesisTicks);
        tag.putInt("ChainsTicks", chainsTicks);
        if (telekinesisTarget != null) {
            tag.putInt("TelekinesisTargetId", telekinesisTarget.getId());
        }
        if (chainsTarget != null) {
            tag.putInt("ChainsTargetId", chainsTarget.getId());
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        teleportCooldown = tag.getInt("TeleportCooldown");
        invisibilityCooldown = tag.getInt("InvisibilityCooldown");
        telekinesisCooldown = tag.getInt("TelekinesisCooldown");
        chainsCooldown = tag.getInt("ChainsCooldown");
        portalSwordsCooldown = tag.getInt("PortalSwordsCooldown");
        iceSpikeCooldown = tag.getInt("IceSpikeCooldown");
        fireballCooldown = tag.getInt("FireballCooldown");
        gravityCooldown = tag.getInt("GravityCooldown");
        isInvisible = tag.getBoolean("IsInvisible");
        invisibilityTicks = tag.getInt("InvisibilityTicks");
        telekinesisTicks = tag.getInt("TelekinesisTicks");
        chainsTicks = tag.getInt("ChainsTicks");
        
        // Восстанавливаем ссылки на цели при загрузке
        if (tag.contains("TelekinesisTargetId")) {
            Entity entity = this.level().getEntity(tag.getInt("TelekinesisTargetId"));
            if (entity instanceof LivingEntity) {
                telekinesisTarget = (LivingEntity) entity;
            }
        }
        if (tag.contains("ChainsTargetId")) {
            Entity entity = this.level().getEntity(tag.getInt("ChainsTargetId"));
            if (entity instanceof LivingEntity) {
                chainsTarget = (LivingEntity) entity;
            }
        }
    }
    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean isHurt = super.hurt(source, amount);
        if (isHurt) {
            LivingEntity attacker = KnightUtils.resolveAttacker(source);
            if (attacker != null && attacker != this && !KnightUtils.isKnight(attacker)) {
                this.setTarget(attacker);
            }
        }
        return isHurt;
    }

}
