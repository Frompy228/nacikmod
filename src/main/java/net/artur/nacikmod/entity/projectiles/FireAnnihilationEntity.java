package net.artur.nacikmod.entity.projectiles;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.artur.nacikmod.registry.ModEntities;

import javax.annotation.Nullable;
import java.util.UUID;

import static java.nio.file.Files.getOwner;

public class FireAnnihilationEntity extends Entity {

    private static final double RADIUS = 10.0D;
    public static final float DAMAGE = 20.0F;
    private static final int FIRE_SECONDS = 6;
    private double life = 2.5;

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

        if (--life <= 0) {
            discard();
        }
    }

    private void damageEntities() {
        AABB box = new AABB(
                getX() - RADIUS, getY() - RADIUS, getZ() - RADIUS,
                getX() + RADIUS, getY() + RADIUS, getZ() + RADIUS
        );

        LivingEntity owner = getOwner();


        for (Entity e : level().getEntities(this, box)) {
            if (e instanceof LivingEntity living) {
                if (owner != null && living == owner) continue;


                living.hurt(damageSources().inFire(), (float) (DAMAGE * 0.4));
                living.hurt(damageSources().indirectMagic(this, owner), (float) (DAMAGE * 0.6));
                living.setSecondsOnFire(FIRE_SECONDS);
                if (owner != null && living instanceof Mob mob) {
                    mob.setTarget(owner);
                }

            }
        }
    }

    @Nullable
    public LivingEntity getOwner() {
        if (ownerUUID == null) return null;
        if (level() instanceof ServerLevel serverLevel) {
            Entity e = serverLevel.getEntity(ownerUUID);
            if (e instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
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


