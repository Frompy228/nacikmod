package net.artur.nacikmod.entity.custom;


import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.entity.custom.LeonidEntity;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;
import java.util.EnumSet;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundEvents;

public class SpartanEntity extends HeroSouls {
    private static final double ATTACK_RANGE = 3.75D;
    private int attackCooldown = 0;
    private static final int ATTACK_COOLDOWN_TICKS = 20;
    private int shieldBlockCooldown = 0;
    private boolean shieldBlockedHit = false;
    private static final int SHIELD_BLOCK_COOLDOWN = 140; // 7 секунд (20 тиков * 7)

    public SpartanEntity(EntityType<? extends HeroSouls> entityType, Level level) {
        super(entityType, level);
        this.setCanUseBothHands(false);
        this.setAttackRange(ATTACK_RANGE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(ModAttributes.BONUS_ARMOR.get(), 5)
                .add(Attributes.ARMOR, 5)
                .add(Attributes.ARMOR_TOUGHNESS, 5)
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.ATTACK_DAMAGE, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(ForgeMod.SWIM_SPEED.get(), 2)
                .add(Attributes.FOLLOW_RANGE, 35.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SpearAttackGoal(this, 1D));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, LeonidEntity.class).setAlertOthers(SpartanEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, this::isValidTarget));
    }

    private boolean isValidTarget(LivingEntity entity) {
        return !(entity instanceof Animal) &&
               !(entity instanceof WaterAnimal) &&
               !(entity instanceof LeonidEntity) &&
               !(entity instanceof SpartanEntity);
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        if (entity instanceof LeonidEntity || entity instanceof SpartanEntity) {
            return true;
        }
        return super.isAlliedTo(entity);
    }

    static class SpearAttackGoal extends Goal {
        private final SpartanEntity spartan;
        private final double speedModifier;

        public SpearAttackGoal(SpartanEntity spartan, double speedModifier) {
            this.spartan = spartan;
            this.speedModifier = speedModifier;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = spartan.getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public void tick() {
            LivingEntity target = spartan.getTarget();
            if (target == null) return;

            spartan.getNavigation().moveTo(target, speedModifier);
            spartan.getLookControl().setLookAt(target, 30.0F, 30.0F);

            double distanceSqr = spartan.distanceToSqr(target);
            if (distanceSqr <= ATTACK_RANGE * ATTACK_RANGE && spartan.hasLineOfSight(target)) {
                if (spartan.attackCooldown <= 0) {
                    spartan.swing(InteractionHand.MAIN_HAND);
                    target.hurt(spartan.damageSources().mobAttack(spartan), 
                            (float) spartan.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    spartan.attackCooldown = ATTACK_COOLDOWN_TICKS;
                } else {
                    spartan.attackCooldown--;
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (shieldBlockCooldown > 0) {
            shieldBlockCooldown--;
            if (shieldBlockCooldown <= 0) {
                shieldBlockedHit = false;
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        // Проверяем, что урон приходит спереди
        Entity attacker = source.getEntity();
        if (attacker != null && this.isUsingItem() && this.getUsedItemHand() == InteractionHand.OFF_HAND &&
                this.getOffhandItem().getItem() == ModItems.LEONID_SHIELD.get() && !shieldBlockedHit) {

            // Проверяем, что атакующий находится спереди
            Vec3 attackerPos = attacker.position();
            Vec3 thisPos = this.position();
            Vec3 lookVec = this.getLookAngle();
            Vec3 toAttacker = attackerPos.subtract(thisPos).normalize();

            // Если атакующий спереди (угол меньше 90 градусов)
            if (lookVec.dot(toAttacker) > 0) {
                // Уменьшаем урон на 80%
                amount *= 0.2f;

                // Отмечаем, что щит заблокировал удар
                shieldBlockedHit = true;
                shieldBlockCooldown = SHIELD_BLOCK_COOLDOWN;
                this.stopUsingItem();

                // Проигрываем звук блокирования
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.SHIELD_BLOCK,
                        this.getSoundSource(), 1.0F, 1.0F);
            }
        }

        boolean isHurt = super.hurt(source, amount);

        if (isHurt) {
            if (attacker instanceof LivingEntity livingAttacker && livingAttacker != this) {
                this.setTarget(livingAttacker);

                // Поднимаем щит при получении урона, если щит не на перезарядке
                if (this.getOffhandItem().getItem() == ModItems.LEONID_SHIELD.get() && shieldBlockCooldown <= 0) {
                    this.startUsingItem(InteractionHand.OFF_HAND);
                }
            }
        }

        return isHurt;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                      MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                      @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);

        // Выдаём экипировку при спавне
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.SPEAR.get()));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.LEONID_SHIELD.get()));

        AttributeInstance attribute = this.getAttribute(ModAttributes.BONUS_ARMOR.get());
        attribute.setBaseValue(5.0);
        return data;
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
