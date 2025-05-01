package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

public class FireArrowEntity extends Projectile {
    private static final float SPEED = 1.5f;
    private float damage = 5.0f;

    public FireArrowEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    public FireArrowEntity(Level level, LivingEntity shooter) {
        super(ModEntities.FIRE_ARROW.get(), level);
        this.setOwner(shooter);
        this.setPos(shooter.getX(), shooter.getEyeY(), shooter.getZ());
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide) {
            Vec3 vec3 = this.getDeltaMovement();
            HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            if (hitresult.getType() != HitResult.Type.MISS) {
                this.onHit(hitresult);
            }
        }

        Vec3 vec3 = this.getDeltaMovement();
        this.setPos(this.getX() + vec3.x, this.getY() + vec3.y, this.getZ() + vec3.z);
        
        // Update rotation based on movement
        if (vec3.lengthSqr() > 0.0D) {
            this.setYRot((float) (Mth.atan2(vec3.x, vec3.z) * (double) (180F / (float) Math.PI)));
            
            double horizontalDistance = Math.sqrt(vec3.x * vec3.x + vec3.z * vec3.z);
            this.setXRot((float) (Mth.atan2(vec3.y, horizontalDistance) * (double) (180F / (float) Math.PI)));
        }
        
        if (this.level().isClientSide) {
            // Add particle effects here if needed
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.hurt(this.damageSources().thrown(this, this.getOwner()), damage);
            livingEntity.setSecondsOnFire(5);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!this.level().isClientSide) {
            this.discard();
        }
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 vec3 = new Vec3(x, y, z).normalize().scale(velocity * SPEED);
        this.setDeltaMovement(vec3);
        
        // Set initial rotation based on direction
        this.setYRot((float) (Mth.atan2(vec3.x, vec3.z) * (double) (180F / (float) Math.PI)));
        double horizontalDistance = Math.sqrt(vec3.x * vec3.x + vec3.z * vec3.z);
        this.setXRot((float) (Mth.atan2(vec3.y, horizontalDistance) * (double) (180F / (float) Math.PI)));
    }
}
