package net.artur.nacikmod.entity.projectiles;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;

public class BloodShootProjectile extends ThrowableItemProjectile {
    private final float damage;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 200; // 10 seconds (20 ticks * 10)
    private static final int EFFECT_DURATION = 100; // 5 seconds (20 ticks * 5)

    public BloodShootProjectile(Level level, LivingEntity shooter, float damage) {
        super(ModEntities.BLOOD_SHOOT_PROJECTILE.get(), shooter, level);
        this.damage = damage;
        this.setNoGravity(true);
    }

    public BloodShootProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
        this.damage = 0;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BLOOD_SHOOT.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!this.level().isClientSide) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                // First apply effect
                if (!livingEntity.hasEffect(ModEffects.BLOOD_EXPLOSION.get())) {
                    livingEntity.addEffect(new MobEffectInstance(
                        ModEffects.BLOOD_EXPLOSION.get(),
                        EFFECT_DURATION,
                        0, // Always level 0
                        true,
                        false // show particles
                    ));
                }
                
                // Then apply damage
                livingEntity.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
            }
        }
        // Не вызываем super.onHitEntity, чтобы снаряд продолжал лететь
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            // Уничтожаем снаряд только при столкновении с блоком
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                this.discard();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();


        // Increment lifetime and check if projectile should be removed
        if (!this.level().isClientSide) {
            lifetime++;
            if (lifetime >= MAX_LIFETIME) {
                this.discard();
            }
        }
    }

    @Override
    public boolean isInWater() {
        return false; // Disable water resistance
    }

    @Override
    public boolean fireImmune() {
        return true;
    }
}
