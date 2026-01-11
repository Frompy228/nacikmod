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
import net.minecraft.network.chat.Component;

public class ManaSwordProjectile extends ThrowableItemProjectile {
    private final float manaDamage;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 200; // 10 seconds (20 ticks * 10)

    public ManaSwordProjectile(Level level, LivingEntity shooter, float manaDamage) {
        super(ModEntities.MANA_SWORD_PROJECTILE.get(), shooter, level);
        this.manaDamage = manaDamage;
        this.setNoGravity(true);
    }

    public ManaSwordProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
        this.manaDamage = 0;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MANA_SWORD.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (!this.level().isClientSide) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                // Base damage (8.0) + mana bonus damage
                float totalDamage = 3.0f + manaDamage;
                livingEntity.hurt(this.damageSources().thrown(this, this.getOwner()), totalDamage);
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            this.discard();
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
