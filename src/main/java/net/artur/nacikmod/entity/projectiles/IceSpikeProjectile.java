package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModBlocks;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class IceSpikeProjectile extends ThrowableItemProjectile {
    private final float damage;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 200; // 10 секунд (20 тиков * 10)

    public IceSpikeProjectile(Level level, LivingEntity shooter, float damage) {
        super(ModEntities.ICE_SPIKE_PROJECTILE.get(), shooter, level);
        this.damage = damage;
    }

    public IceSpikeProjectile(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
        this.damage = 0;
    }

    @Override
    protected Item getDefaultItem() {
        return null;
    }


    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!this.level().isClientSide) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                float totalDamage = 15.0f + damage;
                livingEntity.hurt(this.damageSources().thrown(this, this.getOwner()), totalDamage);
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 2));
                BlockPos center = livingEntity.blockPosition();
                Level level = livingEntity.level();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        for (int dy = 0; dy <= 2; dy++) {
                            if (Math.abs(dx) == 1 || Math.abs(dz) == 1 || dy == 2) {
                                BlockPos pos = center.offset(dx, dy, dz);
                                if (level.isEmptyBlock(pos)) {
                                    level.setBlockAndUpdate(pos, net.artur.nacikmod.registry.ModBlocks.TEMPORARY_ICE.get().defaultBlockState());
                                }
                            }
                        }
                    }
                }
                this.discard();
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult); // это вызывает onHitEntity, если попали в сущность

        if (!this.level().isClientSide) {
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = BlockPos.containing(hitResult.getLocation());
                if (!this.level().getBlockState(pos).is(Blocks.ICE) && !this.level().getBlockState(pos).isAir() && !this.level().getBlockState(pos).is(ModBlocks.TEMPORARY_ICE.get())) {
                    this.discard();
                }
            }
        }
    }


    @Override
    public void tick() {
        super.tick();
        this.setNoGravity(true);
        if (!this.level().isClientSide) {
            lifetime++;
            if (lifetime >= MAX_LIFETIME) {
                this.discard();
                return;
            }

            // Ручная проверка столкновений с сущностями
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2))) {
                if (entity != this.getOwner() && entity.isAlive()) {
                    // Проверяем, не был ли уже нанесён урон
                    this.onHitEntity(new EntityHitResult(entity));
                    break; // Только по одной сущности за тик
                }
            }
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
}
