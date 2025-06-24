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
import net.artur.nacikmod.registry.ModEntities;

import java.util.List;

public class FireCloudEntity extends Projectile {
    private static final int LIFETIME = 200; // 10 seconds (20 ticks * 10)
    private static final double CLOUD_RADIUS = 3.0; // Радиус облака
    private static final float EXPLOSION_DAMAGE = 6.0f; // Урон взрыва
    private static final int FIRE_DURATION = 5; // Длительность поджигания в секундах
    private static final int PARTICLES_PER_TICK = 20; // Количество частиц в тик
    private static final int EXPLOSION_COOLDOWN = 20; // Перезарядка взрыва в тиках (1 секунда = 20 тиков)
    
    private int age = 0;
    private int lastExplosionTick = -EXPLOSION_COOLDOWN; // Начинаем с возможности взрыва

    public FireCloudEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public FireCloudEntity(Level level, LivingEntity owner, double x, double y, double z) {
        super(ModEntities.FIRE_CLOUD.get(), level);
        this.setOwner(owner);
        this.setPos(x, y, z);
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        // Не нужно синхронизировать данные
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide) {
            age++;
            
            // Проверяем, не истекло ли время жизни
            if (age >= LIFETIME) {
                this.discard();
                return;
            }
            
            // Создаем частицы огня
            createFireParticles();
            
            // Проверяем, есть ли сущности в облаке и можно ли взорваться
            if (age - lastExplosionTick >= EXPLOSION_COOLDOWN) {
                checkForEntitiesInCloud();
            }
        }
    }

    private void createFireParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                // Случайная позиция в пределах радиуса облака
                double angle = this.level().getRandom().nextDouble() * 2 * Math.PI;
                double radius = this.level().getRandom().nextDouble() * CLOUD_RADIUS;
                
                double particleX = this.getX() + radius * Math.cos(angle);
                double particleY = this.getY() + (this.level().getRandom().nextDouble() - 0.5) * 2;
                double particleZ = this.getZ() + radius * Math.sin(angle);
                
                // Создаем частицы огня
                serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    particleX, particleY, particleZ,
                    1, // количество
                    0.1, 0.1, 0.1, // скорость
                    0.01 // скорость частиц
                );
                
                // Добавляем дым для эффекта
                if (this.level().getRandom().nextInt(3) == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        particleX, particleY, particleZ,
                        1,
                        0.05, 0.05, 0.05,
                        0.01
                    );
                }
            }
        }
    }

    private void checkForEntitiesInCloud() {
        // Создаем область для проверки сущностей
        AABB cloudArea = new AABB(
            this.getX() - CLOUD_RADIUS, this.getY() - 1, this.getZ() - CLOUD_RADIUS,
            this.getX() + CLOUD_RADIUS, this.getY() + 2, this.getZ() + CLOUD_RADIUS
        );
        
        List<Entity> entities = this.level().getEntities(this, cloudArea);
        
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity && entity != this.getOwner()) {
                // Проверяем, находится ли сущность в радиусе облака
                double distance = this.distanceTo(entity);
                if (distance <= CLOUD_RADIUS) {
                    // Взрываем облако
                    explode();
                    lastExplosionTick = age;
                    break; // Взрываемся только один раз за тик
                }
            }
        }
    }

    private void explode() {
        if (!this.level().isClientSide) {
            // Создаем взрыв (не ломает блоки)
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), 1.5f, false, Level.ExplosionInteraction.NONE);
            
            // Наносим урон сущностям в радиусе
            AABB explosionArea = new AABB(
                this.getX() - CLOUD_RADIUS, this.getY() - 1, this.getZ() - CLOUD_RADIUS,
                this.getX() + CLOUD_RADIUS, this.getY() + 2, this.getZ() + CLOUD_RADIUS
            );
            
            List<Entity> entities = this.level().getEntities(this, explosionArea);
            
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity && entity != this.getOwner()) {
                    double distance = this.distanceTo(entity);
                    if (distance <= CLOUD_RADIUS) {
                        // Наносим урон
                        livingEntity.hurt(this.damageSources().explosion(this, this.getOwner()), EXPLOSION_DAMAGE);
                        
                        // Поджигаем цель
                        livingEntity.setSecondsOnFire(FIRE_DURATION);
                        
                        // Создаем дополнительные частицы взрыва
                        if (this.level() instanceof ServerLevel serverLevel) {
                            for (int i = 0; i < 10; i++) {
                                double particleX = entity.getX() + (this.level().getRandom().nextDouble() - 0.5) * 2;
                                double particleY = entity.getY() + this.level().getRandom().nextDouble() * entity.getBbHeight();
                                double particleZ = entity.getZ() + (this.level().getRandom().nextDouble() - 0.5) * 2;
                                
                                serverLevel.sendParticles(
                                    ParticleTypes.FLAME,
                                    particleX, particleY, particleZ,
                                    1,
                                    0.2, 0.2, 0.2,
                                    0.05
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        // Не вызываем стандартную обработку, так как у нас своя логика
    }

    @Override
    protected void onHit(HitResult hitResult) {
        // Не вызываем стандартную обработку, так как у нас своя логика
    }

    @Override
    public boolean isInWater() {
        return false; // Облако не должно исчезать в воде
    }
} 