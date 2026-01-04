package net.artur.nacikmod.entity.projectiles;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class FirePillarEntity extends Entity {
    private static final double PILLAR_WIDTH = 1.5; // 3 блока ширины (1.5 с каждой стороны от центра)
    private static final double PILLAR_HEIGHT = 15.0; // 15 блоков высоты
    private static final float FIRE_DAMAGE = 35.0f; // 35 урона огнем
    private static final int PARTICLES_PER_TICK = 50; // Количество частиц в тик
    private static final int DAMAGE_COOLDOWN = 20; // Урон каждую секунду (20 тиков)
    private static final int MAX_LIFETIME = 900; // 60 секунд жизни (1200 тиков)
    
    private int age = 0;
    private int lastDamageTick = -DAMAGE_COOLDOWN;
    private java.util.UUID ownerUUID;

    public FirePillarEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public FirePillarEntity(Level level, double x, double y, double z, LivingEntity owner) {
        super(net.artur.nacikmod.registry.ModEntities.FIRE_PILLAR.get(), level);
        this.setPos(x, y, z);
        this.setNoGravity(true);
        this.ownerUUID = owner != null ? owner.getUUID() : null;
    }

    @Override
    protected void defineSynchedData() {
        // Не нужно синхронизировать данные
    }

    @Override
    public void tick() {
        super.tick();
        
        // Обновляем bounding box на основе позиции (getY() - центр столба)
        double halfHeight = PILLAR_HEIGHT / 2.0;
        this.setBoundingBox(new AABB(
            this.getX() - PILLAR_WIDTH, this.getY() - halfHeight, this.getZ() - PILLAR_WIDTH,
            this.getX() + PILLAR_WIDTH, this.getY() + halfHeight, this.getZ() + PILLAR_WIDTH
        ));
        
        // Убеждаемся, что сущность не двигается
        this.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
        
        if (!this.level().isClientSide) {
            age++;
            
            // Проверяем время жизни
            if (age >= MAX_LIFETIME) {
                this.discard();
                return;
            }
            
            // Создаем частицы огня и лавы
            createFireParticles();
            
            // Наносим урон сущностям в столбе каждую секунду
            if (age - lastDamageTick >= DAMAGE_COOLDOWN) {
                damageEntitiesInPillar();
                lastDamageTick = age;
            }
        }
    }

    private void createFireParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            // Создаем частицы по всей площади и высоте столба
            double halfHeight = PILLAR_HEIGHT / 2.0;
            for (int i = 0; i < PARTICLES_PER_TICK; i++) {
                // Случайная позиция в пределах столба (3x3 блока, высота 15, центр на getY())
                double offsetX = (this.level().getRandom().nextDouble() - 0.5) * 3.0;
                double offsetY = (this.level().getRandom().nextDouble() - 0.5) * PILLAR_HEIGHT;
                double offsetZ = (this.level().getRandom().nextDouble() - 0.5) * 3.0;
                
                double particleX = this.getX() + offsetX;
                double particleY = this.getY() + offsetY;
                double particleZ = this.getZ() + offsetZ;
                
                // Основные частицы огня
                serverLevel.sendParticles(
                    ParticleTypes.FLAME,
                    particleX, particleY, particleZ,
                    1,
                    0.1, 0.2, 0.1,
                    0.02
                );
                
                // Добавляем частицы лавы для красоты (каждая 3-я частица)
                if (this.level().getRandom().nextInt(3) == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.LAVA,
                        particleX, particleY, particleZ,
                        1,
                        0.05, 0.1, 0.05,
                        0.01
                    );
                }
                
                // Добавляем дым для эффекта
                if (this.level().getRandom().nextInt(5) == 0) {
                    serverLevel.sendParticles(
                        ParticleTypes.LARGE_SMOKE,
                        particleX, particleY, particleZ,
                        1,
                        0.1, 0.1, 0.1,
                        0.01
                    );
                }
            }
        }
    }

    private void damageEntitiesInPillar() {
        // Создаем область столба (3x3 блока, высота 15, центр на getY())
        double halfHeight = PILLAR_HEIGHT / 2.0;
        AABB pillarArea = new AABB(
            this.getX() - PILLAR_WIDTH, this.getY() - halfHeight, this.getZ() - PILLAR_WIDTH,
            this.getX() + PILLAR_WIDTH, this.getY() + halfHeight, this.getZ() + PILLAR_WIDTH
        );
        
        List<Entity> entities = this.level().getEntities(this, pillarArea);
        
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                // Пропускаем владельца
                if (ownerUUID != null && entity.getUUID().equals(ownerUUID)) {
                    continue;
                }
                
                // Проверяем, находится ли сущность в пределах столба
                double dx = entity.getX() - this.getX();
                double dz = entity.getZ() - this.getZ();
                double distance = Math.sqrt(dx * dx + dz * dz);
                
                if (distance <= PILLAR_WIDTH && 
                    entity.getY() >= this.getY() - halfHeight && 
                    entity.getY() <= this.getY() + halfHeight) {
                    
                    // Наносим урон огнем
                    livingEntity.hurt(this.damageSources().onFire(), FIRE_DAMAGE);
                    
                    // Поджигаем цель на 5 секунд
                    livingEntity.setSecondsOnFire(5);
                    
                    // Создаем дополнительные частицы при нанесении урона
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

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        if (compound.contains("Age")) {
            this.age = compound.getInt("Age");
        }
        if (compound.contains("LastDamageTick")) {
            this.lastDamageTick = compound.getInt("LastDamageTick");
        }
        if (compound.contains("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        compound.putInt("Age", this.age);
        compound.putInt("LastDamageTick", this.lastDamageTick);
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
    }

    // Сущность невосприимчива к огню
    @Override
    public boolean fireImmune() {
        return true;
    }

    // Сущность не может быть сдвинута
    @Override
    public boolean isPushable() {
        return false;
    }

    // Сущность не может сталкиваться с блоками
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }
}

