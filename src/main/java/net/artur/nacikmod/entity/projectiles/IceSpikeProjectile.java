package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModBlocks;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.util.KnightUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class IceSpikeProjectile extends ThrowableItemProjectile {

    private final float damage;
    private int lifetime = 0;
    private static final int MAX_LIFETIME = 200; // 10 секунд (20 тиков * 10)

    public IceSpikeProjectile(Level level, LivingEntity shooter, float damage) {
        super(ModEntities.ICE_SPIKE_PROJECTILE.get(), shooter, level);
        this.damage = damage;
        this.setNoGravity(true); // снаряд летит прямо
    }

    public IceSpikeProjectile(EntityType<? extends ThrowableItemProjectile> type, Level level) {
        super(type, level);
        this.damage = 0;
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return null; // снаряд не связан с предметом
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

            // Ручная проверка столкновений с сущностями
            for (LivingEntity entity : this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.2))) {
                if (entity != this.getOwner() && entity.isAlive()) {
                    this.onHitEntity(new EntityHitResult(entity));
                    break; // Только по одной сущности за тик
                }
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.level().isClientSide) return;

        if (result.getEntity() instanceof LivingEntity living) {
            if (KnightUtils.isKnight(living)) {
                this.discard();
                return;
            }
            float totalDamage = 20.0f + damage;
            living.hurt(this.damageSources().thrown(this, this.getOwner()), totalDamage);
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 2));
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 2));

            // Создание TEMPORARY_ICE вокруг цели
            BlockPos center = living.blockPosition();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    for (int dy = 0; dy <= 2; dy++) {
                        if (Math.abs(dx) == 1 || Math.abs(dz) == 1 || dy == 2) {
                            BlockPos pos = center.offset(dx, dy, dz);
                            if (level().isEmptyBlock(pos)) {
                                level().setBlockAndUpdate(pos, ModBlocks.TEMPORARY_ICE.get().defaultBlockState());
                            }
                        }
                    }
                }
            }
        }

        this.discard(); // снаряд исчезает после попадания в сущность
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (this.level().isClientSide) return;

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = BlockPos.containing(hitResult.getLocation());
            // Если блок не лёд, TEMPORARY_ICE или воздух — снаряд останавливается
            if (!level().getBlockState(pos).is(Blocks.ICE) &&
                    !level().getBlockState(pos).is(ModBlocks.TEMPORARY_ICE.get()) &&
                    !level().getBlockState(pos).isAir()) {
                this.discard();
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
