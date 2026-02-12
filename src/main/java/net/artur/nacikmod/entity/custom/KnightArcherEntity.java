package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.entity.projectiles.KnightArrowEntity;
import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class KnightArcherEntity extends HeroSouls {

    // ================= CONSTANTS =================

    private static final int BONUS_ARMOR = 10;
    private static final int MAX_MANA = 5500;

    private static final int SHOOT_COOLDOWN = 30;
    private static final int DODGE_COOLDOWN = 80;
    private static final int DRAW_TIME = 20; // тики натяжения лука

    // ================= STATE =================

    private int shootCooldown;
    private int dodgeCooldown;
    private int drawTicks;
    private boolean isDrawing;

    // ================= SHOT TYPES =================

    private enum ArcherShotType {
        NORMAL,
        POISON,
        POWER
    }

    // ================= CONSTRUCTOR =================

    public KnightArcherEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    // ================= ATTRIBUTES =================

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(ModAttributes.BONUS_ARMOR.get(), BONUS_ARMOR)
                .add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.MOVEMENT_SPEED, 0.30)
                .add(Attributes.ATTACK_DAMAGE, 6)
                .add(Attributes.ARMOR, 10)
                .add(Attributes.ARMOR_TOUGHNESS, 6)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .add(Attributes.FOLLOW_RANGE, 40);
    }

    // ================= GOALS =================

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        goalSelector.addGoal(2, new ArcherCombatGoal(this));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 12));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(
                this, Mob.class, 10, true, false, this::isValidTarget
        ));
    }

    private boolean isValidTarget(LivingEntity e) {
        return !(e instanceof Animal)
                && !(e instanceof WaterAnimal)
                && !(e instanceof KnightArcherEntity)
                && !(e instanceof KnightEntity)
                && !(e instanceof KnightLeaderEntity);
    }



    @Override
    public void tick() {
        super.tick();
        if (shootCooldown > 0) shootCooldown--;
        if (dodgeCooldown > 0) dodgeCooldown--;
    }

    // ================= COMBAT GOAL =================

    // ================= COMBAT GOAL =================
    static class ArcherCombatGoal extends Goal {
        private final KnightArcherEntity archer;
        private ArcherShotType currentShot;

        private static final int AIR_SHOOT_COOLDOWN = 100; // 5 секунд (20 тиков = 1 секунда)
        private int airJumpCooldown = 0;

        ArcherCombatGoal(KnightArcherEntity archer) {
            this.archer = archer;
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return archer.getTarget() != null && archer.getTarget().isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = archer.getTarget();

            if (target == null || !target.isAlive()) {
                resetBow();
                return;
            }

            if (!archer.hasLineOfSight(target)) {
                resetBow();
                return;
            }

            double dist = archer.distanceTo(target);
            archer.getLookControl().setLookAt(target, 30, 30);

            // =================== Движение ===================
            if (dist < 8) {
                retreat(target);
                dodge();
            } else {
                archer.getNavigation().moveTo(target, 1.0);
            }

            // =================== Прыжок в воздухе ===================
            if (airJumpCooldown > 0) airJumpCooldown--;

            if (archer.onGround() && airJumpCooldown == 0 && archer.shootCooldown < 5) {
                archer.setDeltaMovement(archer.getDeltaMovement().add(0, 0.6, 0)); // прыжок
                airJumpCooldown = AIR_SHOOT_COOLDOWN;
            }

            // =================== Стрельба ===================
            if (dist <= 25) {
                handleShooting(target);
            }
        }

        private void resetBow() {
            if (archer.isDrawing) {
                archer.stopUsingItem();
                archer.isDrawing = false;
                archer.drawTicks = 0;
            }
        }

        // =================== Движение ===================
        private void retreat(LivingEntity target) {
            Vec3 dir = archer.position().subtract(target.position());
            dir = new Vec3(dir.x, 0, dir.z); // игнорируем высоту для обычного отступления

            if (dir.lengthSqr() == 0) return;

            Vec3 motion = dir.normalize().scale(0.35);

            // Если цель ниже, добавляем контролируемый прыжок
            double deltaY = target.getY() - archer.getY();
            if (deltaY < -1.0 && archer.onGround()) {
                motion = motion.add(0, 0.4, 0);
            }

            archer.setDeltaMovement(motion.add(0, archer.getDeltaMovement().y, 0));
        }

        private void dodge() {
            if (archer.dodgeCooldown > 0) return;

            Vec3 side = archer.getLookAngle().cross(new Vec3(0, 1, 0)).normalize();
            if (archer.random.nextBoolean()) side = side.scale(-1);

            Vec3 motion = new Vec3(side.x * 0.6, 0, side.z * 0.6);
            archer.setDeltaMovement(archer.getDeltaMovement().add(motion));
            archer.dodgeCooldown = DODGE_COOLDOWN;
        }

        // =================== Стрельба ===================
        private void handleShooting(LivingEntity target) {
            if (archer.shootCooldown > 0) return;

            if (!archer.isDrawing) {
                archer.isDrawing = true;
                archer.drawTicks = 0;
                currentShot = chooseShotType();

                archer.startUsingItem(InteractionHand.MAIN_HAND);

                if (currentShot == ArcherShotType.POWER) playPowerTelegraph();
                return;
            }

            archer.drawTicks++;

            if (archer.drawTicks >= DRAW_TIME) {
                fireArrow(target, currentShot);

                archer.stopUsingItem();
                archer.swing(InteractionHand.MAIN_HAND);

                archer.isDrawing = false;
                archer.drawTicks = 0;
                archer.shootCooldown = 15; // быстрее стрельба
            }
        }

        private void fireArrow(LivingEntity target, ArcherShotType type) {
            Arrow arrow = new KnightArrowEntity(archer.level(), archer);

            Vec3 from = archer.getEyePosition();
            Vec3 to = target.getEyePosition().subtract(from);

            arrow.setPos(from);
            arrow.shoot(to.x, to.y, to.z, 3.2F, 0.3F); // сильнее и точнее

            applyShotEffects(arrow, type);
            spawnArrowVisuals(arrow, type);

            archer.level().addFreshEntity(arrow);
        }

        private ArcherShotType chooseShotType() {
            float r = archer.random.nextFloat();
            if (r < 0.25F) return ArcherShotType.POISON;
            if (r < 0.40F) return ArcherShotType.POWER;
            return ArcherShotType.NORMAL;
        }

        private void applyShotEffects(Arrow arrow, ArcherShotType type) {
            switch (type) {
                case POISON:
                    arrow.addEffect(new MobEffectInstance(MobEffects.POISON, 80));
                    arrow.setBaseDamage(arrow.getBaseDamage() + 4); // Урон для стрелы яда
                    break;
                case POWER:
                    arrow.setBaseDamage(arrow.getBaseDamage() + 7); // Увеличенный урон для стрелы силы
                    break;
                case NORMAL:
                default:
                    arrow.setBaseDamage(arrow.getBaseDamage() + 3); // Для обычной стрелы добавляем +3 к урону
                    break;
            }
            arrow.setCustomName(Component.literal(type.name()));
        }

        private void spawnArrowVisuals(Arrow arrow, ArcherShotType type) {
            Level level = archer.level();
            if (type == ArcherShotType.POISON) {
                level.addParticle(ParticleTypes.ENTITY_EFFECT, arrow.getX(), arrow.getY(), arrow.getZ(), 0, 1, 0);
            } else if (type == ArcherShotType.POWER) {
                level.addParticle(ParticleTypes.CRIT, arrow.getX(), arrow.getY(), arrow.getZ(), 0.3, 0.3, 0.3);
            }
        }

        private void playPowerTelegraph() {
            Level level = archer.level();
            level.playSound(null, archer.blockPosition(),
                    SoundEvents.CROSSBOW_LOADING_END, SoundSource.HOSTILE, 0.8F, 0.8F);
        }
    }

    // ================= SPAWN =================

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag tag) {

        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, tag);

        equipBow(world.getRandom());

        getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            mana.setMaxMana(MAX_MANA);
            mana.setMana(MAX_MANA);
        });

        getAttribute(ModAttributes.BONUS_ARMOR.get()).setBaseValue(BONUS_ARMOR);
        return data;
    }

    private void equipBow(RandomSource random) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
    }



    @Override
    public boolean hurt(DamageSource source, float amount) {
        LivingEntity attacker = net.artur.nacikmod.util.KnightUtils.resolveAttacker(source);
        if (attacker != null && net.artur.nacikmod.util.KnightUtils.isKnight(attacker)) {
            return false;
        }

        boolean isHurt = super.hurt(source, amount);
        if (isHurt && attacker != null && attacker != this && !net.artur.nacikmod.util.KnightUtils.isKnight(attacker)) {
            this.setTarget(attacker);
        }
        return isHurt;
    }


    @Override protected void dropEquipment() {}
    @Override protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {}
    @Override protected void dropExperience() {}

}