package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CrossProjectile extends Projectile {
    private static final float DAMAGE = 30.0f;
    private static final int MAX_LIFETIME = 200; // 10 секунд (20 тиков * 10)
    private int lifetime = 0;

    public CrossProjectile(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public CrossProjectile(Level level, LivingEntity shooter) {
        this(ModEntities.CROSS_PROJECTILE.get(), level);
        this.setOwner(shooter);
    }

    @Override
    protected void defineSynchedData() {
        // Не требуется синхронизация данных
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        // Исключаем владельца из проверки столкновений
        return super.canHitEntity(entity) && entity != this.getOwner();
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide) {
            lifetime++;
            if (lifetime >= MAX_LIFETIME) {
                this.discard();
                return;
            }

            // Проверяем столкновение с блоками вручную (как в FireHailEntity)
            Vec3 newPos = this.position().add(this.getDeltaMovement());
            BlockPos blockPos = new BlockPos((int)newPos.x, (int)newPos.y, (int)newPos.z);
            
            if (this.level().getBlockState(blockPos).isSolidRender(this.level(), blockPos)) {
                // Разрушаем блок при столкновении
                if (this.level() instanceof ServerLevel serverLevel) {
                    // Разрушаем блок (как взрыв, но без взрыва)
                    this.level().destroyBlock(blockPos, false);
                }
                // Столкновение с блоком
                this.onHit(new BlockHitResult(newPos, this.getDirection(), blockPos, false));
                return;
            }
            
            // Ручная проверка столкновений с сущностями
            // Исключаем владельца из проверки - снаряд проходит сквозь него
            for (LivingEntity entity : this.level().getEntitiesOfClass(
                    LivingEntity.class, 
                    this.getBoundingBox().inflate(0.5))) {
                if (entity != this.getOwner() && entity.isAlive() && !entity.isInvulnerable()) {
                    this.onHitEntity(new EntityHitResult(entity));
                    break; // Только по одной сущности за тик
                }
            }
        }
        
        // КРИТИЧЕСКИ ВАЖНО: Обновляем позицию вручную в конце (как в FireHailEntity)
        // Без этого снаряд будет застывать в воздухе!
        this.setPos(this.getX() + this.getDeltaMovement().x, 
                   this.getY() + this.getDeltaMovement().y, 
                   this.getZ() + this.getDeltaMovement().z);
        
        // Обновляем поворот на основе направления движения
        Vec3 vec3 = this.getDeltaMovement();
        if (vec3.lengthSqr() > 0.0D) {
            this.setYRot((float) (Mth.atan2(vec3.x, vec3.z) * (double) (180F / (float) Math.PI)));
            double horizontalDistance = Math.sqrt(vec3.x * vec3.x + vec3.z * vec3.z);
            this.setXRot((float) (Mth.atan2(vec3.y, horizontalDistance) * (double) (180F / (float) Math.PI)));
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!this.level().isClientSide) {
            Entity entity = entityHitResult.getEntity();
            
            // Владелец уже исключен из проверки в tick(), но на всякий случай проверяем
            if (entity == this.getOwner()) {
                return; // Просто пропускаем, не удаляем снаряд
            }
            
            if (entity instanceof LivingEntity livingEntity) {
                // Наносим урон
                livingEntity.hurt(this.damageSources().magic(), DAMAGE);
            }
            this.discard();
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (!this.level().isClientSide) {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                BlockPos pos = blockHitResult.getBlockPos();
                
                // Разрушаем блок при столкновении
                if (this.level() instanceof ServerLevel serverLevel) {
                    this.level().destroyBlock(pos, false);
                }
            }
            this.discard();
        }
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    /**
     * Выстреливает снаряд в указанном направлении с заданной скоростью
     */
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 vec3 = new Vec3(x, y, z).normalize();
        if (inaccuracy > 0.0F) {
            vec3 = vec3.add(
                this.random.nextGaussian() * 0.0075 * (double)inaccuracy,
                this.random.nextGaussian() * 0.0075 * (double)inaccuracy,
                this.random.nextGaussian() * 0.0075 * (double)inaccuracy
            );
            vec3 = vec3.normalize();
        }
        
        vec3 = vec3.scale(velocity);
        this.setDeltaMovement(vec3);
        
        // Устанавливаем поворот снаряда по направлению движения
        double horizontalDistance = Math.sqrt(vec3.x * vec3.x + vec3.z * vec3.z);
        this.setYRot((float)(Mth.atan2(vec3.x, vec3.z) * (double)(180F / (float)Math.PI)));
        this.setXRot((float)(Mth.atan2(vec3.y, horizontalDistance) * (double)(180F / (float)Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        
        // Убеждаемся, что снаряд имеет импульс движения
        this.hasImpulse = true;
    }
}

