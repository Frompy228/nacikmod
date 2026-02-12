package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
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
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;

public class KnightEntity extends HeroSouls {
    private static final double ATTACK_RANGE = 3.2D;
    private int attackCooldown = 0;
    private static final int ATTACK_COOLDOWN_TICKS = 17;
    private int shieldBlockCooldown = 0;
    private boolean shieldBlockedHit = false;
    private static final int SHIELD_BLOCK_COOLDOWN = 140;
    private static final int BONUS_ARMOR = 12;

    private static final int JUMP_COOLDOWN_TICKS = 60;
    private static final double VERTICAL_JUMP_THRESHOLD = 2.0;

    private int jumpCooldown = 0;
    private static final int MAX_MANA = 5000;

    // Синхронизация данных для анимации
    private static final EntityDataAccessor<Boolean> IS_DRINKING = SynchedEntityData.defineId(KnightEntity.class, EntityDataSerializers.BOOLEAN);

    private static final int POTION_COOLDOWN_TICKS = 300;
    private static final int DRINK_DURATION = 32;
    private int potionCooldown = 0;

    private static final UUID DRINK_SPEED_UUID = UUID.fromString("5CD17E52-A79A-43D3-A529-90FDE04B181E");
    private static final AttributeModifier DRINK_SPEED_PENALTY = new AttributeModifier(DRINK_SPEED_UUID, "Drinking slow", -0.25D, AttributeModifier.Operation.ADDITION);

    public KnightEntity(EntityType<? extends HeroSouls> entityType, Level level) {
        super(entityType, level);
        this.setCanUseBothHands(true);
        this.setAttackRange(ATTACK_RANGE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(ModAttributes.BONUS_ARMOR.get(), BONUS_ARMOR)
                .add(Attributes.ARMOR, 15)
                .add(Attributes.ARMOR_TOUGHNESS, 7)
                .add(Attributes.MAX_HEALTH, 50.0)
                .add(Attributes.ATTACK_DAMAGE, 9.0)
                .add(Attributes.MOVEMENT_SPEED, 0.27)
                .add(ForgeMod.SWIM_SPEED.get(), 2)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 35.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_DRINKING, false);
    }

    public boolean isDrinking() {
        return this.entityData.get(IS_DRINKING);
    }

    public void setDrinking(boolean drinking) {
        this.entityData.set(IS_DRINKING, drinking);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(3, new KnightDrinkPotionGoal(this));
        this.goalSelector.addGoal(4, new KnightMeleeAttackGoal(this, 1.0D));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers(KnightEntity.class));
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
        if (jumpCooldown > 0) jumpCooldown--;
        if (potionCooldown > 0) potionCooldown--;

        if (shieldBlockCooldown > 0) {
            shieldBlockCooldown--;
            if (shieldBlockCooldown <= 0) shieldBlockedHit = false;
        }

        LivingEntity target = this.getTarget();
        if (target != null && !this.isDrinking()) {
            checkAndPerformJump(target);
        }
    }

    // --- GOALS ---

    static class KnightMeleeAttackGoal extends Goal {
        private final KnightEntity knight;
        private final double speedModifier;

        public KnightMeleeAttackGoal(KnightEntity knight, double speedModifier) {
            this.knight = knight;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = knight.getTarget();
            return target != null && target.isAlive() && !knight.isDrinking();
        }

        @Override
        public void tick() {
            LivingEntity target = knight.getTarget();
            if (target == null) return;

            knight.getNavigation().moveTo(target, speedModifier);
            knight.getLookControl().setLookAt(target, 30.0F, 30.0F);

            double distanceSqr = knight.distanceToSqr(target);
            if (distanceSqr <= ATTACK_RANGE * ATTACK_RANGE && knight.hasLineOfSight(target)) {
                if (knight.attackCooldown <= 0) {
                    knight.swing(InteractionHand.MAIN_HAND);
                    target.hurt(knight.damageSources().mobAttack(knight), (float) knight.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    knight.attackCooldown = ATTACK_COOLDOWN_TICKS;
                } else {
                    knight.attackCooldown--;
                }
            }
        }
    }

    static class KnightDrinkPotionGoal extends Goal {
        private final KnightEntity knight;
        private int drinkTimer;
        private ItemStack oldMainHand = ItemStack.EMPTY;

        public KnightDrinkPotionGoal(KnightEntity knight) {
            this.knight = knight;
        }

        @Override
        public boolean canUse() {
            return knight.getHealth() < knight.getMaxHealth() * 0.5f && knight.potionCooldown <= 0 && !knight.isDrinking();
        }

        @Override
        public void start() {
            knight.setDrinking(true);
            this.drinkTimer = DRINK_DURATION;
            this.oldMainHand = knight.getMainHandItem().copy();

            ItemStack potion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRONG_HEALING);
            knight.setItemSlot(EquipmentSlot.MAINHAND, potion);
            knight.startUsingItem(InteractionHand.MAIN_HAND);

            AttributeInstance speed = knight.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.addTransientModifier(DRINK_SPEED_PENALTY);

            knight.level().playSound(null, knight.getX(), knight.getY(), knight.getZ(), SoundEvents.WITCH_DRINK, SoundSource.HOSTILE, 1.0f, 1.0f);
        }

        @Override
        public void tick() {
            if (--drinkTimer <= 0) {
                this.finishDrinking();
            }
        }

        private void finishDrinking() {
            PotionUtils.getMobEffects(knight.getMainHandItem()).forEach(effect -> knight.addEffect(new MobEffectInstance(effect)));
            knight.stopUsingItem();
            knight.setItemSlot(EquipmentSlot.MAINHAND, oldMainHand);

            AttributeInstance speed = knight.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speed != null) speed.removeModifier(DRINK_SPEED_PENALTY);

            knight.setDrinking(false);
            knight.potionCooldown = POTION_COOLDOWN_TICKS;
        }

        @Override
        public boolean canContinueToUse() {
            return knight.isDrinking() && drinkTimer > 0;
        }
    }

    // --- СПОСОБНОСТИ ---

    private void checkAndPerformJump(LivingEntity target) {
        if (jumpCooldown > 0 || !this.onGround()) return;
        double heightDifference = target.getY() - this.getY();
        if (heightDifference > VERTICAL_JUMP_THRESHOLD && this.distanceTo(target) <= 5.0) {
            if (this.hasLineOfSight(target)) performJump();
        }
    }

    private void performJump() {
        double jumpPower = 0.45 + (this.random.nextDouble() * 0.2);
        this.setDeltaMovement(this.getDeltaMovement().add(0, jumpPower, 0));
        jumpCooldown = JUMP_COOLDOWN_TICKS;
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_ATTACK_STRONG, SoundSource.HOSTILE, 0.5F, 1.2F);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && this.isUsingItem() && this.getUsedItemHand() == InteractionHand.OFF_HAND &&
                this.getOffhandItem().getItem() instanceof ShieldItem && !shieldBlockedHit) {

            Vec3 lookVec = this.getLookAngle();
            Vec3 toAttacker = attacker.position().subtract(this.position()).normalize();

            if (lookVec.dot(toAttacker) > 0) {
                amount *= 0.35f;
                shieldBlockedHit = true;
                shieldBlockCooldown = SHIELD_BLOCK_COOLDOWN;
                this.stopUsingItem();
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SHIELD_BLOCK, SoundSource.HOSTILE, 1.0F, 0.9F);
            }
        }

        boolean isHurt = super.hurt(source, amount);
        if (isHurt) {
            LivingEntity resolved = net.artur.nacikmod.util.KnightUtils.resolveAttacker(source);
            if (resolved != null && resolved != this && !net.artur.nacikmod.util.KnightUtils.isKnight(resolved)) {
                this.setTarget(resolved);
            }
            if (this.getOffhandItem().getItem() instanceof ShieldItem && shieldBlockCooldown <= 0) {
                this.startUsingItem(InteractionHand.OFF_HAND);
            }
        }
        return isHurt;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);
        equipRandomWeaponAndShield(world.getRandom());
        this.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            mana.setMaxMana(MAX_MANA);
            mana.setMana(MAX_MANA);
        });
        this.getAttribute(ModAttributes.BONUS_ARMOR.get()).setBaseValue(BONUS_ARMOR);
        return data;
    }

    private void equipRandomWeaponAndShield(RandomSource random) {
        int r = random.nextInt(5);
        if (r == 0) this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.KNIGHT_SWORD.get()));
        else if (r == 1) this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        else if (r == 2) this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SPEAR.get()));
        else if (r == 3) this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        else this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_SWORD));

        if (random.nextFloat() < 0.5f) {
            this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
        }
    }

    @Override protected void dropEquipment() {}
    @Override protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {}
    @Override protected void dropExperience() {}
}