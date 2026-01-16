package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.artur.nacikmod.registry.ModItems;

public class FireBallEntity extends ThrowableItemProjectile {
    private static final float DAMAGE = 85.0F;
    private static final float EXPLOSION_RADIUS = 4.0F;
    private static final int FIRE_DURATION = 6;
    private static final int MAX_LIFETIME = 100;

    private int lifetime = 0;

    public FireBallEntity(Level level, LivingEntity shooter) {
        super(ModEntities.FIREBALL.get(), shooter, level);
        this.setNoGravity(true);
    }

    public FireBallEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.FIRE_BALL.get(); // .get() для получения Item
    }

    @Override
    public void tick() {
        super.tick(); // ThrowableItemProjectile САМ двигает!

        if (!this.level().isClientSide) {
            lifetime++;
            if (lifetime >= MAX_LIFETIME) {
                explode();
                this.discard();
                return;
            }

            // Частицы
            if (level() instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.FLAME,
                        getX(), getY(), getZ(), 3,
                        0.1, 0.1, 0.1, 0.01);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (level().isClientSide) return;

        if (result.getEntity() instanceof LivingEntity target) {
            // Получаем владельца, если он LivingEntity
            LivingEntity owner = (getOwner() instanceof LivingEntity le) ? le : null;

            // Пропускаем владельца
            if (target == owner) return;

            // 50% огненный урон
            target.hurt(this.damageSources().inFire(), DAMAGE * 0.4f);

            // 50% магический урон, передаём владельца
            target.hurt(this.damageSources().indirectMagic(this, owner), DAMAGE * 0.6f);

            // Поджигаем цель
            target.setSecondsOnFire(FIRE_DURATION);

        }

        explode();
        this.discard();
    }



    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            explode();
            this.discard();
        }
    }

    private void explode() {
        Level level = level();

        // Двойной урон! (наш + стандартный взрыв)
        level.explode(this, getX(), getY(), getZ(),
                EXPLOSION_RADIUS, true, Level.ExplosionInteraction.BLOCK);

        // Поджог
        BlockPos center = BlockPos.containing(getX(), getY(), getZ());
        int radius = (int)EXPLOSION_RADIUS;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = center.offset(x, y, z);
                    if (level.isEmptyBlock(pos) &&
                            Blocks.FIRE.canSurvive(level.getBlockState(pos), level, pos) &&
                            random.nextFloat() < 0.5f) {
                        level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
            }
        }

        // Частицы
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(),
                    30, 0.8, 0.8, 0.8, 0.1);
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean isInWater() {
        return false;
    }

    public void shoot(Vec3 direction, float speed) {
        this.setDeltaMovement(direction.normalize().scale(speed));
    }
}