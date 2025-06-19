package net.artur.nacikmod.entity.projectiles;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;

public class ManaArrowProjectile extends ThrowableItemProjectile {
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 200; // 10 seconds (20 ticks * 10)
    private static final float BASE_DAMAGE = 5F;
    private static final float GRAVITY = 0.03F; // Небольшая гравитация

    public ManaArrowProjectile(Level level, LivingEntity shooter) {
        super(ModEntities.MANA_ARROW.get(), shooter, level);
        this.setNoGravity(false); // Включаем гравитацию
    }

    public ManaArrowProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(false); // Включаем гравитацию
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.MANA_CRYSTAL.get();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!this.level().isClientSide) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                // Получаем скорость стрелы
                Vec3 velocity = this.getDeltaMovement();
                double speed = velocity.length() * 1.4;
                
                // Рассчитываем урон на основе скорости
                float damage = (float) (BASE_DAMAGE * speed);
                
                // Применяем урон и откидывание
                livingEntity.hurt(this.damageSources().thrown(this, this.getOwner()), damage);

                // Накладываем эффект замедления на 2 секунды (40 тиков)
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 0));
                
                // Используем встроенный механизм откидывания
                if (!this.isNoGravity()) {
                    double knockbackStrength = 0.5D * speed * 0.7; // Базовый множитель откидывания
                    livingEntity.knockback(knockbackStrength, 
                        -velocity.x, // Инвертируем направление для отталкивания
                        -velocity.z);
                }
            }
        }
        this.discard();
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        this.discard();
    }

    @Override
    public void tick() {
        super.tick();
        
        // Применяем гравитацию
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0, -GRAVITY, 0));
        }

        if (!this.level().isClientSide) {
            lifetime++;
            if (lifetime >= MAX_LIFETIME) {
                this.discard();
            }
        }
    }

    @Override
    public boolean isInWater() {
        return false;
    }
}
