package net.artur.nacikmod.entity.projectiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.artur.nacikmod.registry.ModEntities;

import java.util.UUID;

public class FireAnnihilationEntity extends Entity {

    private static final double RADIUS = 10.0D;
    private static final float DAMAGE = 15.0F;
    private static final int FIRE_SECONDS = 6;

    private UUID ownerUUID;

    public FireAnnihilationEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public FireAnnihilationEntity(Level level, LivingEntity owner,
                                  double x, double y, double z) {
        this(ModEntities.FIRE_ANNIHILATION.get(), level);
        this.ownerUUID = owner.getUUID();
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData() {}

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) {
            ownerUUID = tag.getUUID("Owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUUID != null) {
            tag.putUUID("Owner", ownerUUID);
        }
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            damageEntities();
            igniteBlocks();
        } else {
            spawnParticlesClient();
        }

        // 💀 1 тик — исчезает
        discard();
    }

    private void damageEntities() {
        AABB box = new AABB(
                getX() - RADIUS, getY() - RADIUS, getZ() - RADIUS,
                getX() + RADIUS, getY() + RADIUS, getZ() + RADIUS
        );

        for (Entity e : level().getEntities(this, box)) {
            if (e instanceof LivingEntity living) {
                if (ownerUUID != null && living.getUUID().equals(ownerUUID)) continue;

                living.hurt(damageSources().indirectMagic(this, null), DAMAGE);
                living.setSecondsOnFire(FIRE_SECONDS);
            }
        }
    }

    private void igniteBlocks() {
        BlockPos center = blockPosition();
        int r = 4;

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-r, -1, -r),
                center.offset(r, 2, r)
        )) {
            if (level().isEmptyBlock(pos) && random.nextFloat() < 0.15f) {
                level().setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
            }
        }
    }

    private void spawnParticlesClient() {
        for (int i = 0; i < 120; i++) {
            level().addParticle(
                    ParticleTypes.FLAME,
                    getX() + random.nextGaussian() * 3,
                    getY() + random.nextDouble() * 2,
                    getZ() + random.nextGaussian() * 3,
                    0, 0.02, 0
            );
        }
    }
}


