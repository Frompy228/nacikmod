package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import javax.annotation.Nullable;
import java.util.List;

public class FireWallDamageZone extends Entity {
    private static final EntityDataAccessor<Integer> DATA_PARENT_ID =
            SynchedEntityData.defineId(FireWallDamageZone.class, EntityDataSerializers.INT);

    @Nullable
    private FireWallEntity parent;

    public FireWallDamageZone(FireWallEntity parent) {
        this(ModEntities.FIRE_WALL_DAMAGE_ZONE.get(), parent.level(), parent);
    }

    public FireWallDamageZone(EntityType<? extends FireWallDamageZone> entityType, Level level) {
        this(entityType, level, null);
    }

    private FireWallDamageZone(EntityType<? extends FireWallDamageZone> entityType, Level level, @Nullable FireWallEntity parent) {
        super(entityType, level);
        this.noPhysics = true;
        if (parent != null) {
            setParent(parent);
        }
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_PARENT_ID, -1);
    }

    @Override
    public void tick() {
        super.tick();

        resolveParentIfNeeded();

        if (parent == null || !parent.isAlive()) {
            this.discard();
            return;
        }

        syncWithParent();

        if (!this.level().isClientSide) {
            applyAreaDamage();
        }
    }

    void syncWithParent() {
        if (parent == null) {
            return;
        }
        Vec3 center = parent.getWallCenter();
        this.setPos(center.x, parent.getY(), center.z);
        updateBoundingBox();
    }

    private void applyAreaDamage() {
        if (parent == null) {
            return;
        }

        List<LivingEntity> entities = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox(),
                LivingEntity::isAlive);

        for (LivingEntity livingEntity : entities) {
            parent.tryDamageTarget(livingEntity);
        }
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void updateBoundingBox() {
        Vec3 center = parent.getWallCenter();
        Vec3 right = parent.getWallRight();
        Vec3 normal = parent.getWallNormal();

        double halfWidth = FireWallEntity.WALL_WIDTH / 2.0D;
        double halfThickness = FireWallEntity.DAMAGE_ZONE_THICKNESS / 2.0D;
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;

        double[][] combinations = {
                {-halfWidth, -halfThickness},
                {-halfWidth, halfThickness},
                {halfWidth, -halfThickness},
                {halfWidth, halfThickness}
        };

        for (double[] combo : combinations) {
            Vec3 corner = center.add(right.scale(combo[0])).add(normal.scale(combo[1]));
            minX = Math.min(minX, corner.x);
            maxX = Math.max(maxX, corner.x);
            minZ = Math.min(minZ, corner.z);
            maxZ = Math.max(maxZ, corner.z);
        }

        double minY = parent.getY();
        double maxY = parent.getY() + FireWallEntity.WALL_HEIGHT;
        this.setBoundingBox(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    private void setParent(FireWallEntity parent) {
        this.parent = parent;
        if (!this.level().isClientSide) {
            this.entityData.set(DATA_PARENT_ID, parent.getId());
        }
    }

    private void resolveParentIfNeeded() {
        if (parent != null) {
            return;
        }

        int parentId = this.entityData.get(DATA_PARENT_ID);
        if (parentId <= 0) {
            return;
        }

        Entity entity = this.level().getEntity(parentId);
        if (entity instanceof FireWallEntity fireWallEntity) {
            this.parent = fireWallEntity;
        }
    }
}

