package net.artur.nacikmod.entity.custom;

import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.registry.ModAttributes;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.EnumSet;
import java.util.List;

public class LanserEntity extends HeroSouls {

    private boolean attackWithMainHand = true; // Флаг для чередования атак
    private int manaRegenTimer = 0; // Таймер для регенерации маны


    public LanserEntity(EntityType<? extends HeroSouls> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void tick() {
        super.tick();

        // Получаем ми
        Level world = this.level();
        if (world == null || world.isClientSide) return; // Проверка на null и клиент

        // Радиус, в котором проверяем взгляд
        double radius = 10.0;

        // Получаем список ближайших игроков и мобов, исключая самого Лансера
        List<LivingEntity> nearbyEntities = world.getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(radius),
                entity -> (entity instanceof Player || entity instanceof Mob) && entity != this
        );

        for (LivingEntity entity : nearbyEntities) {
            // Проверяем, смотрит ли сущность прямо на голову Лансера
            if (isLookingAtLanser(entity)) {
                entity.addEffect(new MobEffectInstance(ModEffects.LOVE.get(), 12, 0)); // 10 секунд эффекта LOVE
            }
        }
    }



    // Метод для проверки, смотрит ли сущность на голову Лансера
    private boolean isLookingAtLanser(LivingEntity entity) {
        Vec3 entityViewVec = entity.getViewVector(1.0F).normalize(); // Вектор направления взгляда сущности
        Vec3 lanserPosVec = new Vec3(this.getX(), this.getY() + this.getEyeHeight(), this.getZ()); // Координаты головы Лансера
        Vec3 directionToLanser = lanserPosVec.subtract(entity.getEyePosition()).normalize(); // Вектор к голове Лансера

        double dotProduct = entityViewVec.dot(directionToLanser); // Скалярное произведение

        return dotProduct > 0.98 && this.hasLineOfSight(entity); // Проверяем угол (почти 1) и наличие прямого взгляда

    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty,
                                        MobSpawnType reason, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData data = super.finalizeSpawn(world, difficulty, reason, spawnData, dataTag);

        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.LANS_OF_NACII.get()));
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModItems.LANS_OF_PROTECTION.get()));

        return data;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(ModAttributes.BONUS_ARMOR.get(),15)
                .add(Attributes.ARMOR, 20)
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.8)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new RetreatOnLowHealthGoal(this)); // Рывок назад при низком ХП
        this.goalSelector.addGoal(2, new CustomMeleeAttackGoal(this, 1.0)); // Атака
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.5)); // Блуждание
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false, this::isValidTarget));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }


    static class RetreatOnLowHealthGoal extends Goal {
        private final LanserEntity lanser;
        private int cooldown = 0;

        public RetreatOnLowHealthGoal(LanserEntity lanser) {
            this.lanser = lanser;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return lanser.getHealth() <= 25 && cooldown <= 0 && lanser.getTarget() != null;
        }

        @Override
        public void start() {
            LivingEntity target = lanser.getTarget();
            if (target == null) return;

            // Направление рывка — от цели
            Vec3 retreatDirection = lanser.position().subtract(target.position()).normalize().scale(1.5);
            lanser.setDeltaMovement(retreatDirection.x, 0.3, retreatDirection.z); // Добавляем импульс
            lanser.hurtMarked = true; // Обновляем движение
            cooldown = 60; // 3 секунды перезарядка (60 тиков)
        }

        @Override
        public void tick() {
            if (cooldown > 0) cooldown--;
        }
    }


    static class CustomMeleeAttackGoal extends MeleeAttackGoal {
        private final LanserEntity lanser;
        private int attackCooldown = 0;
        private int dashCooldown = 0; // Перезарядка рывка
        private final double preferredDistance = 4.0; // Желаемая дистанция

        public CustomMeleeAttackGoal(LanserEntity mob, double speedModifier) {
            super(mob, speedModifier, true);
            this.lanser = mob;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public void tick() {
            super.tick();
            LivingEntity target = lanser.getTarget();
            if (target == null || target instanceof Animal || target instanceof WaterAnimal) return;

            double squaredDistance = lanser.distanceToSqr(target);
            double minDistance = preferredDistance - 0.5;
            double maxDistance = preferredDistance + 0.5;
            double dashDistance = 10.0; // Расстояние для рывка (10 блоков)

            if (squaredDistance < minDistance * minDistance) {
                // Отступаем, если слишком близко
                Vec3 retreatDirection = lanser.position().subtract(target.position()).normalize().scale(0.8);
                lanser.getNavigation().moveTo(
                        lanser.getX() + retreatDirection.x,
                        lanser.getY(),
                        lanser.getZ() + retreatDirection.z,
                        0.5
                );
            } else if (squaredDistance > maxDistance * maxDistance) {
                // Если слишком далеко – приближаемся
                lanser.getNavigation().moveTo(target, 0.5);
            }

            // Если цель находится примерно в 10 блоках, совершаем рывок
            if (dashCooldown <= 0 && squaredDistance >= (dashDistance - 2) * (dashDistance - 2) && squaredDistance <= dashDistance * dashDistance) {
                performDash(target);
                dashCooldown = 80; // Перезарядка рывка (4 секунды)
            } else if (dashCooldown > 0) {
                dashCooldown--;
            }

            // Если можем атаковать – атакуем
            if (attackCooldown <= 0 && squaredDistance <= maxDistance * maxDistance) {
                InteractionHand attackHand = lanser.attackWithMainHand ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
                lanser.swing(attackHand);
                lanser.doHurtTarget(target);

                ItemStack weapon = lanser.getItemInHand(attackHand);
                applyWeaponEffects(lanser, target, weapon);

                lanser.attackWithMainHand = !lanser.attackWithMainHand; // Меняем руку
                attackCooldown = 20;
            } else {
                attackCooldown--;
            }
        }

        // Метод для совершения рывка
        private void performDash(LivingEntity target) {
            Vec3 dashDirection = target.position().subtract(lanser.position()).normalize().scale(1.5);
            lanser.setDeltaMovement(dashDirection.x, 0.3, dashDirection.z); // Поднимаем чуть вверх
            lanser.hurtMarked = true; // Обновляем движение
        }

        private void applyWeaponEffects(LanserEntity lanser, LivingEntity target, ItemStack weapon) {
            if (weapon.getItem() == ModItems.LANS_OF_NACII.get()) {
                double damageDealt = target.getMaxHealth() - target.getHealth();
                int amplifier = (int) Math.floor(damageDealt / 3);
                if (amplifier > 0) {
                    target.addEffect(new MobEffectInstance(ModEffects.HEALTH_REDUCTION.get(), 20000, amplifier));
                }
                target.addEffect(new MobEffectInstance(ModEffects.NO_REGEN.get(), 200, 0));
            }

            if (weapon.getItem() == ModItems.LANS_OF_PROTECTION.get()) {
                double baseDamage = lanser.getAttributeValue(Attributes.ATTACK_DAMAGE) + 1;
                double armor = target.getArmorValue();
                double bonusDamage = baseDamage * (armor / 20.0);
                double totalDamage = baseDamage + bonusDamage;
                target.hurt(lanser.damageSources().mobAttack(lanser), (float) totalDamage);

                MobEffectInstance existingEffect = target.getEffect(ModEffects.ARMOR_REDUCTION.get());
                int newAmplifier = (int) Math.floor(totalDamage / 10);
                if (existingEffect != null) {
                    newAmplifier += existingEffect.getAmplifier();
                }
                target.addEffect(new MobEffectInstance(ModEffects.ARMOR_REDUCTION.get(), 200, newAmplifier));
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean isHurt = super.hurt(source, amount);

        if (isHurt) {
            Entity attacker = source.getEntity();
            if (attacker instanceof LivingEntity livingAttacker && livingAttacker != this) {
                this.setTarget(livingAttacker); // Меняем цель на атакующего
            }
        }
        return isHurt;
    }


    private boolean isValidTarget(LivingEntity entity) {
        return !(entity instanceof Animal) &&  // Исключаем наземных животных
                !(entity instanceof WaterAnimal); // Исключаем рыб и водных существ
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);

        // Дропаем два разных предмета, каждый ровно 1 раз
        this.spawnAtLocation(new ItemStack(ModItems.LANS_OF_PROTECTION.get()));
        this.spawnAtLocation(new ItemStack(ModItems.LANS_OF_NACII.get()));
        this.spawnAtLocation(new ItemStack(ModItems.MAGIC_CIRCUIT.get()));
    }


    @Override
    protected SoundEvent getAmbientSound() {
        return super.getAmbientSound();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return super.getHurtSound(damageSource);
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return super.getDeathSound();
    }
}