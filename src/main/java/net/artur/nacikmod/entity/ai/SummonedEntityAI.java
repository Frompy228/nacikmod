package net.artur.nacikmod.entity.ai;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.UUID;

public class SummonedEntityAI extends TargetGoal {
    private final Mob mob;
    private final UUID ownerUUID;
    private final TargetingConditions targetingConditions;

    public SummonedEntityAI(Mob mob, UUID ownerUUID) {
        super(mob, false);
        this.mob = mob;
        this.ownerUUID = ownerUUID;
        this.targetingConditions = TargetingConditions.forCombat()
                .range(16.0)
                .selector(entity -> {
                    // Don't target the owner
                    if (entity instanceof Player player && player.getUUID().equals(ownerUUID)) {
                        return false;
                    }
                    
                    // Don't target animals
                    if (entity instanceof Animal) {
                        return false;
                    }
                    
                    // Don't target fish (check class name)
                    String entityClassName = entity.getClass().getSimpleName().toLowerCase();
                    if (entityClassName.contains("fish") || entityClassName.contains("salmon") || 
                        entityClassName.contains("cod") || entityClassName.contains("pufferfish") ||
                        entityClassName.contains("tropical")) {
                        return false;
                    }
                    
                    // Don't target other summoned entities
                    if (entity.getTags().contains("lord_of_souls_summoned")) {
                        return false;
                    }
                    
                    // Target everything else
                    return true;
                });
        setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        if (!mob.getTags().contains("lord_of_souls_summoned")) {
            return false;
        }
        
        // Find the owner
        Player owner = findOwner();
        if (owner == null) {
            return false;
        }
        
        // Look for targets near the owner
        LivingEntity target = findTargetNearOwner(owner);
        if (target != null) {
            mob.setTarget(target);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (!mob.getTags().contains("lord_of_souls_summoned")) {
            return false;
        }
        
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        
        // Check if target is still valid
        if (!targetingConditions.test(mob, target)) {
            mob.setTarget(null);
            return false;
        }
        
        return true;
    }

    private Player findOwner() {
        Level level = mob.level();
        for (Player player : level.players()) {
            if (player.getUUID().equals(ownerUUID)) {
                return player;
            }
        }
        return null;
    }

    private LivingEntity findTargetNearOwner(Player owner) {
        double searchRadius = 16.0;
        return owner.level().getNearestEntity(
            owner.level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(searchRadius)),
            targetingConditions,
            owner,
            owner.getX(),
            owner.getY(),
            owner.getZ()
        );
    }
} 