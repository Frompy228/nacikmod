package net.artur.nacikmod.entity.projectiles;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.artur.nacikmod.registry.ModEntities;

import java.util.List;

public class FireHailEntity extends Projectile {
    private static final int LIFETIME = 60; // 3 секунды (20 тиков * 3)
    private static final float DAMAGE = 30.0f;
    private static final int FIRE_DURATION = 5;
    private int age = 0;

    public FireHailEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(false); // Падает вниз
        this.setDeltaMovement(0, -0.7, 0); // Устанавливаем скорость падения
    }

    public FireHailEntity(Level level, LivingEntity target) {
        super(ModEntities.FIRE_HAIL.get(), level);
        if (target != null) {
            this.setPos(target.getX(), target.getY() + 8, target.getZ()); // Появляется над целью
        }
        this.setDeltaMovement(0, -0.7, 0); // Падает вниз
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide) {
            age++;
            if (age >= LIFETIME) {
                this.discard();
                return;
            }
            
            // Применяем гравитацию
            Vec3 currentMotion = this.getDeltaMovement();
            this.setDeltaMovement(currentMotion.x, currentMotion.y - 0.04, currentMotion.z);
            
            // Проверяем столкновение с блоками
            Vec3 newPos = this.position().add(this.getDeltaMovement());
            BlockPos blockPos = new BlockPos((int)newPos.x, (int)newPos.y, (int)newPos.z);
            
            if (this.level().getBlockState(blockPos).isSolidRender(this.level(), blockPos)) {
                // Столкновение с блоком
                this.onHit(new BlockHitResult(newPos, this.getDirection(), blockPos, false));
                return;
            }
            
            // Ручная проверка столкновений с сущностями (как в IceSpikeProjectile)
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2))) {
                if (entity != this.getOwner() && entity.isAlive()) {
                    this.onHitEntity(new EntityHitResult(entity));
                    break; // Только по одной сущности за тик
                }
            }
            
            // Эффект частиц во время падения
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 2, 0.2, 0.2, 0.2, 0.01);
            }
        }
        
        // Обновляем позицию
        this.setPos(this.getX() + this.getDeltaMovement().x, 
                   this.getY() + this.getDeltaMovement().y, 
                   this.getZ() + this.getDeltaMovement().z);
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!this.level().isClientSide) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                // Используем магический тип урона
                livingEntity.hurt(this.damageSources().magic(), DAMAGE);
                livingEntity.setSecondsOnFire(FIRE_DURATION);
                
                // Дополнительные частицы при попадании
                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLAME, entity.getX(), entity.getY(), entity.getZ(), 5, 0.3, 0.3, 0.3, 0.02);
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!this.level().isClientSide) {
            // Частицы при столкновении с землей
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.LAVA, this.getX(), this.getY(), this.getZ(), 10, 0.5, 0.2, 0.5, 0.05);
            }
            this.discard();
        }
    }

    @Override
    public boolean isInWater() {
        return false;
    }
}
