package net.artur.nacikmod.entity.projectiles;

import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SuppressingGate extends Entity {
    private static final int FALL_DURATION = 40; // 2 seconds to fall (faster)
    private static final int MAX_LIFETIME = 200; // 20 seconds total lifetime
    
    private int fallTicks = 0;
    private int lifetime = 0;
    private boolean hasLanded = false;
    private double targetY;
    private double startY; // Store the actual spawn height
    private float initialYaw = 0.0F;
    private java.util.UUID ownerUUID; // Store the owner's UUID

    public SuppressingGate(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

        public SuppressingGate(Level level, double x, double y, double z, float yaw, net.minecraft.world.entity.player.Player owner) {
        super(ModEntities.SUPPRESSING_GATE.get(), level);
        this.setPos(x, y, z);
        this.startY = y; // Store the actual spawn height
        this.targetY = this.findGroundLevel(x, y, z);
        this.setNoGravity(true);
        
        // Set rotation to face the direction player is looking
        this.setYRot(yaw);
        this.setXRot(0.0F);
        
        // Store the initial yaw for later use
        this.initialYaw = yaw;
        this.ownerUUID = owner.getUUID(); // Store the owner's UUID
    }

    @Override
    public void tick() {
        super.tick();
        

        
        if (!this.level().isClientSide) {
            // Increment lifetime
            lifetime++;
            if (lifetime >= MAX_LIFETIME) {
                this.discard();
                return;
            }
            
            if (!hasLanded) {
                // Falling phase
                fallTicks++;
                
                if (fallTicks >= FALL_DURATION) {
                    // Land on ground
                    this.setPos(this.getX(), targetY, this.getZ());
                    hasLanded = true;
                    this.setDeltaMovement(Vec3.ZERO);
                    // Ensure the entity keeps its initial rotation when landed
                    this.setYRot(initialYaw);
                    this.setXRot(0.0F);
                } else {
                    // Continue falling with smooth interpolation
                    double progress = (double) fallTicks / FALL_DURATION;
                    double currentY = this.startY - (this.startY - targetY) * progress;
                    this.setPos(this.getX(), currentY, this.getZ());
                    // Keep entity facing the initial direction during fall
                    this.setYRot(initialYaw);
                    this.setXRot(0.0F);
                }
            } else {
                // Landed phase - check if block below still exists, if not, start falling again
                BlockPos belowPos = new BlockPos((int) this.getX(), (int) this.getY() - 1, (int) this.getZ());
                if (this.level().getBlockState(belowPos).isAir()) {
                    // Block below was destroyed, start falling again
                    hasLanded = false;
                    fallTicks = 0;
                    this.targetY = this.findGroundLevel(this.getX(), this.getY(), this.getZ());
                } else {
                    // Apply effects to entities touching the gate
                    applyEffectsToTouchingEntities();
                }
            }
        }
    }

    private double findGroundLevel(double x, double spawnY, double z) {
        BlockPos pos = new BlockPos((int) x, (int) spawnY, (int) z);
        
        // Find the highest non-air block starting from spawn Y position
        while (pos.getY() > this.level().getMinBuildHeight() && 
               this.level().getBlockState(pos).isAir()) {
            pos = pos.below();
        }
        
        // If we reached the bottom and still no ground, use the bottom level
        if (pos.getY() <= this.level().getMinBuildHeight()) {
            return this.level().getMinBuildHeight() + 1.0;
        }
        
        return pos.getY() + 1.0; // Place slightly above the ground
    }

    private void applyEffectsToTouchingEntities() {
        // Get all entities that are touching this gate
        AABB gateBox = this.getBoundingBox();
        List<Entity> touchingEntities = this.level().getEntities(this, gateBox);
        
                    for (Entity entity : touchingEntities) {
                if (entity instanceof LivingEntity livingEntity) {
                    // Skip the owner of the gate
                    if (ownerUUID != null && entity.getUUID().equals(ownerUUID)) {
                        continue;
                    }
                    
                    // Apply the suppressing effect - 1 second duration, always refresh
                    livingEntity.addEffect(new MobEffectInstance(
                        ModEffects.SUPPRESSING_GATE.get(),
                        20, // 1 second duration
                        0, // Always level 0
                        false,
                        false,
                        false
                    ));
                }
            }
    }

    @Override
    protected void defineSynchedData() {
        // No synced data needed
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        if (compound.contains("FallTicks")) {
            this.fallTicks = compound.getInt("FallTicks");
        }
        if (compound.contains("HasLanded")) {
            this.hasLanded = compound.getBoolean("HasLanded");
        }
        if (compound.contains("TargetY")) {
            this.targetY = compound.getDouble("TargetY");
        }
        if (compound.contains("Lifetime")) {
            this.lifetime = compound.getInt("Lifetime");
        }
        if (compound.contains("InitialYaw")) {
            this.initialYaw = compound.getFloat("InitialYaw");
        }
        if (compound.contains("StartY")) {
            this.startY = compound.getDouble("StartY");
        }
        if (compound.contains("OwnerUUID")) {
            this.ownerUUID = compound.getUUID("OwnerUUID");
        }
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound) {
        compound.putInt("FallTicks", this.fallTicks);
        compound.putBoolean("HasLanded", this.hasLanded);
        compound.putDouble("TargetY", this.targetY);
        compound.putInt("Lifetime", this.lifetime);
        compound.putFloat("InitialYaw", this.initialYaw);
        compound.putDouble("StartY", this.startY);
        if (this.ownerUUID != null) {
            compound.putUUID("OwnerUUID", this.ownerUUID);
        }
    }

    // Make entity pass through lava without burning
    @Override
    public boolean fireImmune() {
        return true;
    }

    // Make entity pass through water without resistance
    @Override
    public boolean isInWater() {
        return false;
    }

    // Make entity pass through tall grass and other plants
    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    // Make entity ignore block collisions (pass through grass, etc.)
    @Override
    public boolean isPushable() {
        return false;
    }
}
