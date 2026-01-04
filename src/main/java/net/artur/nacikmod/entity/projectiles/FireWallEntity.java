package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraftforge.network.NetworkHooks;

public class FireWallEntity extends Entity {
    public static final double WALL_WIDTH = 3.0D;
    public static final double WALL_HEIGHT = 4.0D;
    public static final double WALL_THICKNESS = 0.1D; // ~1 пиксель
    public static final double DAMAGE_ZONE_THICKNESS = 0.6D;
    public static final int MAX_LIFETIME = 20 * 20; // 20 seconds
    public static final float CONTACT_DAMAGE = 10.0f;
    private static final int FIRE_DURATION_SECONDS = 4;
    private static final int DAMAGE_COOLDOWN_TICKS = 10;

    private final Map<UUID, Integer> recentHits = new HashMap<>();
    @Nullable
    private UUID ownerUUID;
    private int age;
    private FireWallDamageZone damageZone;

    public FireWallEntity(EntityType<? extends FireWallEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public FireWallEntity(Level level, LivingEntity owner, Vec3 spawnPos, float yaw) {
        this(ModEntities.FIRE_WALL.get(), level);
        this.ownerUUID = owner.getUUID();
        this.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        this.setRot(yaw, 0.0F);
        this.setYHeadRot(yaw);
        this.setYBodyRot(yaw);
        updateBoundingBox();
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            spawnClientParticles();
            return;
        }

        if (damageZone == null || !damageZone.isAlive()) {
            damageZone = new FireWallDamageZone(this);
            this.level().addFreshEntity(damageZone);
            damageZone.syncWithParent();
        } else {
            damageZone.syncWithParent();
        }

        age++;
        if (age >= MAX_LIFETIME || !isOwnerValid()) {
            destroyWall();
            return;
        }

        this.setDeltaMovement(Vec3.ZERO);
        updateBoundingBox();
        cleanupHitCooldowns();
        
        // Гарантируем, что стена не горит (на случай, если огонь все-таки установился)
        if (this.getRemainingFireTicks() > 0) {
            this.setRemainingFireTicks(0);
        }
    }

    private boolean shouldDamage(LivingEntity entity) {
        int lastHitTick = recentHits.getOrDefault(entity.getUUID(), -DAMAGE_COOLDOWN_TICKS);
        return (this.tickCount - lastHitTick) >= DAMAGE_COOLDOWN_TICKS;
    }

    private void cleanupHitCooldowns() {
        int cutoff = this.tickCount - DAMAGE_COOLDOWN_TICKS * 2;
        recentHits.entrySet().removeIf(entry -> entry.getValue() < cutoff);
    }

    private boolean isOwner(LivingEntity livingEntity) {
        return ownerUUID != null && ownerUUID.equals(livingEntity.getUUID());
    }

    private boolean isOwnerValid() {
        if (ownerUUID == null) {
            return true;
        }
        return getOwner() != null;
    }

    @Nullable
    public LivingEntity getOwner() {
        if (ownerUUID == null) {
            return null;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUUID);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        this.ownerUUID = owner != null ? owner.getUUID() : null;
    }

    private void spawnClientParticles() {
        Vec3 center = getWallCenter();
        Vec3 right = getWallRight();
        Vec3 normal = getWallNormal();

        for (int i = 0; i < 8; i++) {
            double widthOffset = (this.random.nextDouble() - 0.5D) * WALL_WIDTH;
            double heightOffset = (this.random.nextDouble() - 0.5D) * WALL_HEIGHT;
            double thicknessOffset = (this.random.nextDouble() - 0.5D) * WALL_THICKNESS * 2.0D;

            Vec3 particlePos = center
                    .add(right.scale(widthOffset))
                    .add(0.0D, heightOffset, 0.0D)
                    .add(normal.scale(thicknessOffset));

            this.level().addParticle(ParticleTypes.FLAME, particlePos.x, particlePos.y, particlePos.z, 0.0D, 0.02D, 0.0D);
        }
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("Owner")) {
            this.ownerUUID = compound.getUUID("Owner");
        }
        this.age = compound.getInt("Age");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        if (this.ownerUUID != null) {
            compound.putUUID("Owner", this.ownerUUID);
        }
        compound.putInt("Age", this.age);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void push(Entity entity) {
        super.push(entity);

        if (!this.level().isClientSide && entity instanceof LivingEntity livingEntity) {
            tryDamageTarget(livingEntity);
        }
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    @Override
    public void setRemainingFireTicks(int ticks) {
        // Предотвращаем установку огня - огненная стена не может гореть
        super.setRemainingFireTicks(0);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    Vec3 getWallCenter() {
        return new Vec3(this.getX(), this.getY() + WALL_HEIGHT / 2.0D, this.getZ());
    }

    Vec3 getWallRight() {
        float yawRad = (float) Math.toRadians(this.getYRot());
        double x = Mth.cos(yawRad);
        double z = Mth.sin(yawRad);
        Vec3 vec = new Vec3(x, 0.0D, z);
        return vec.normalize();
    }

    Vec3 getWallNormal() {
        Vec3 right = getWallRight();
        Vec3 normal = new Vec3(-right.z, 0.0D, right.x);
        return normal.normalize();
    }

    private void updateBoundingBox() {
        Vec3 center = getWallCenter();
        Vec3 right = getWallRight();
        Vec3 normal = getWallNormal();

        double halfWidth = WALL_WIDTH / 2.0D;
        double halfThickness = WALL_THICKNESS / 2.0D;
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

        double minY = this.getY();
        double maxY = this.getY() + WALL_HEIGHT;
        this.setBoundingBox(new AABB(minX, minY, minZ, maxX, maxY, maxZ));
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        updateBoundingBox();
    }

    @Override
    public void setYRot(float yRot) {
        super.setYRot(yRot);
        updateBoundingBox();
    }

    @Override
    public void setXRot(float xRot) {
        super.setXRot(xRot);
        updateBoundingBox();
    }

    void tryDamageTarget(LivingEntity livingEntity) {
        if (livingEntity == null || !livingEntity.isAlive()) {
            return;
        }

        if (isOwner(livingEntity) || livingEntity.isInvulnerableTo(this.damageSources().inFire())) {
            return;
        }

        if (shouldDamage(livingEntity)) {
            recentHits.put(livingEntity.getUUID(), this.tickCount);
            livingEntity.hurt(this.damageSources().inFire(), CONTACT_DAMAGE);
            livingEntity.setSecondsOnFire(FIRE_DURATION_SECONDS);
        }
    }

    private void destroyWall() {
        if (!this.level().isClientSide && damageZone != null) {
            damageZone.discard();
        }
        this.discard();
    }

}
