package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireHailEntity extends Projectile {
    private static final int LIFETIME = 60; // 3 секунды (20 тиков * 3)
    private static final float DAMAGE = 30.0f;
    private static final int FIRE_DURATION = 5;

    private int age = 0;

    public FireHailEntity(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false); // Падает вниз
        this.setDeltaMovement(0, -0.7, 0); // Начальная скорость падения
    }

    public FireHailEntity(Level level, LivingEntity target) {
        super(ModEntities.FIRE_HAIL.get(), level);
        if (target != null) {
            this.setPos(target.getX(), target.getY() + 8, target.getZ()); // Появляется над целью
        }
        this.setNoGravity(false);
        this.setDeltaMovement(0, -0.7, 0); // Начальная скорость падения
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            age++;
            if (age >= LIFETIME) {
                this.discard();
                return;
            }

            // Применяем гравитацию
            Vec3 motion = this.getDeltaMovement();
            motion = motion.add(0, -0.04, 0); // стандартная гравитация
            this.setDeltaMovement(motion);

            // Двигаем снаряд через физику (учитывает столкновения)
            this.move(net.minecraft.world.entity.MoverType.SELF, motion);

            // Проверка столкновений с сущностями
            for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2))) {
                if (entity != this.getOwner() && entity.isAlive()) {
                    this.onHitEntity(new EntityHitResult(entity));
                    break; // Только по одной сущности за тик
                }
            }

            // Частицы во время падения
            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        this.getX(), this.getY(), this.getZ(),
                        2, 0.2, 0.2, 0.2, 0.01);
            }
        }

        // Обновляем вращение снаряда по направлению движения
        updateRotation();
    }

    protected void updateRotation() {
        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() > 0.0001D) {
            this.setYRot((float) (Math.atan2(motion.x, motion.z) * (180 / Math.PI)));
            double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
            this.setXRot((float) (Math.atan2(motion.y, horizontal) * (180 / Math.PI)));
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!level().isClientSide) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.hurt(this.damageSources().magic(), DAMAGE);
                livingEntity.setSecondsOnFire(FIRE_DURATION);

                // Частицы попадания
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLAME,
                            entity.getX(), entity.getY(), entity.getZ(),
                            5, 0.3, 0.3, 0.3, 0.02);
                }
            }
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!level().isClientSide) {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHit = (BlockHitResult) hitResult;
                // Частицы при столкновении с землей
                if (level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.LAVA,
                            blockHit.getLocation().x,
                            blockHit.getLocation().y,
                            blockHit.getLocation().z,
                            10, 0.5, 0.2, 0.5, 0.05);
                }
            }
            this.discard();
        }
    }

    @Override
    public boolean isInWater() {
        return false;
    }
}
