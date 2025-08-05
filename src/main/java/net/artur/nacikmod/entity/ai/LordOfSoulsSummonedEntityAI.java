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
import net.artur.nacikmod.capability.lord_of_souls_summoned_entities.LordOfSoulsSummonedEntityProvider;

import java.util.EnumSet;
import java.util.UUID;

public class LordOfSoulsSummonedEntityAI extends TargetGoal {
    private final Mob mob;
    private final UUID ownerUUID;
    private final TargetingConditions targetingConditions;

    public LordOfSoulsSummonedEntityAI(Mob mob, UUID ownerUUID) {
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
                    
                                         // Don't target other summoned entities from the SAME owner
                     boolean isOtherSummoned = false;
                     UUID otherOwnerUUID = null;
                     
                     // Check capability first (if owner is online)
                     for (Player player : mob.level().players()) {
                         if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                                 .map(cap -> cap.isTracked(entity.getUUID())).orElse(false)) {
                             isOtherSummoned = true;
                             otherOwnerUUID = player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                                     .map(cap -> cap.getOwnerUUID(entity.getUUID())).orElse(null);
                             break;
                         }
                     }
                     
                     // Check NBT backup if not found (for offline owners)
                     if (!isOtherSummoned) {
                         var entityData = entity.getPersistentData();
                         if (entityData.contains("lord_of_souls_owner")) {
                             isOtherSummoned = true;
                             try {
                                 otherOwnerUUID = entityData.getUUID("lord_of_souls_owner");
                             } catch (Exception e) {
                                 // Invalid UUID format, skip
                             }
                         }
                     }
                     
                     // Only block if it's from the SAME owner
                     if (isOtherSummoned && otherOwnerUUID != null && otherOwnerUUID.equals(ownerUUID)) {
                         return false;
                     }
                    
                    // Target everything else
                    return true;
                });
        setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        // Check if mob is tracked using capability or NBT backup
        boolean isTracked = false;
        
        // Check capability first (if owner is online)
        for (Player player : mob.level().players()) {
            if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                    .map(cap -> cap.isTracked(mob.getUUID())).orElse(false)) {
                isTracked = true;
                break;
            }
        }
        
        // Check NBT backup if not found (for offline owners)
        if (!isTracked) {
            var mobData = mob.getPersistentData();
            if (mobData.contains("lord_of_souls_owner")) {
                isTracked = true;
            }
        }
        
        if (!isTracked) {
            return false;
        }
        
        // Find the owner
        Player owner = findOwner();
        if (owner == null) {
            return false;
        }
        
        // Clear any existing target that might be the owner
        if (mob.getTarget() instanceof Player targetPlayer && targetPlayer.getUUID().equals(ownerUUID)) {
            mob.setTarget(null);
            return false;
        }
        
        // Additional safety check: if mob is targeting owner, clear it immediately
        if (mob.getTarget() != null && mob.getTarget().getUUID().equals(ownerUUID)) {
            mob.setTarget(null);
            return false;
        }
        
                 // Additional safety check: if mob is targeting another summoned entity from the SAME owner, clear it immediately
         if (mob.getTarget() != null) {
             boolean isTargetSummoned = false;
             UUID targetOwnerUUID = null;
             
             // Check capability first (if owner is online)
             for (Player player : mob.level().players()) {
                 if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                         .map(cap -> cap.isTracked(mob.getTarget().getUUID())).orElse(false)) {
                     isTargetSummoned = true;
                     targetOwnerUUID = player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                             .map(cap -> cap.getOwnerUUID(mob.getTarget().getUUID())).orElse(null);
                     break;
                 }
             }
             
             // Check NBT backup if not found (for offline owners)
             if (!isTargetSummoned) {
                 var targetData = mob.getTarget().getPersistentData();
                 if (targetData.contains("lord_of_souls_owner")) {
                     isTargetSummoned = true;
                     try {
                         targetOwnerUUID = targetData.getUUID("lord_of_souls_owner");
                     } catch (Exception e) {
                         // Invalid UUID format, skip
                     }
                 }
             }
             
             // Only clear if it's from the SAME owner
             if (isTargetSummoned && targetOwnerUUID != null && targetOwnerUUID.equals(ownerUUID)) {
                 mob.setTarget(null);
                 return false;
             }
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
        // Check if mob is tracked using capability or NBT backup
        boolean isTracked = false;
        
        // Check capability first (if owner is online)
        for (Player player : mob.level().players()) {
            if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                    .map(cap -> cap.isTracked(mob.getUUID())).orElse(false)) {
                isTracked = true;
                break;
            }
        }
        
        // Check NBT backup if not found (for offline owners)
        if (!isTracked) {
            var mobData = mob.getPersistentData();
            if (mobData.contains("lord_of_souls_owner")) {
                isTracked = true;
            }
        }
        
        if (!isTracked) {
            return false;
        }
        
        LivingEntity target = mob.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        }
        
        // Additional check: never target the owner
        if (target instanceof Player targetPlayer && targetPlayer.getUUID().equals(ownerUUID)) {
            mob.setTarget(null);
            return false;
        }
        
                 // Additional check: never target other summoned entities from the SAME owner
         boolean isTargetSummoned = false;
         UUID targetOwnerUUID = null;
         
         // Check capability first (if owner is online)
         for (Player player : mob.level().players()) {
             if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                     .map(cap -> cap.isTracked(target.getUUID())).orElse(false)) {
                 isTargetSummoned = true;
                 targetOwnerUUID = player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                         .map(cap -> cap.getOwnerUUID(target.getUUID())).orElse(null);
                 break;
             }
         }
         
         // Check NBT backup if not found (for offline owners)
         if (!isTargetSummoned) {
             var targetData = target.getPersistentData();
             if (targetData.contains("lord_of_souls_owner")) {
                 isTargetSummoned = true;
                 try {
                     targetOwnerUUID = targetData.getUUID("lord_of_souls_owner");
                 } catch (Exception e) {
                     // Invalid UUID format, skip
                 }
             }
         }
         
         // Only clear if it's from the SAME owner
         if (isTargetSummoned && targetOwnerUUID != null && targetOwnerUUID.equals(ownerUUID)) {
             mob.setTarget(null);
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
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        
        // Get all entities near the owner
        var entities = owner.level().getEntitiesOfClass(LivingEntity.class, owner.getBoundingBox().inflate(searchRadius));
        
        for (LivingEntity entity : entities) {
            // Skip if not a valid target according to targeting conditions
            if (!targetingConditions.test(mob, entity)) {
                continue;
            }
            
                         // Additional check: never target other summoned entities from the SAME owner
             boolean isTargetSummoned = false;
             UUID targetOwnerUUID = null;
             
             // Check capability first (if owner is online)
             for (Player player : mob.level().players()) {
                 if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                         .map(cap -> cap.isTracked(entity.getUUID())).orElse(false)) {
                     isTargetSummoned = true;
                     targetOwnerUUID = player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                             .map(cap -> cap.getOwnerUUID(entity.getUUID())).orElse(null);
                     break;
                 }
             }
             
             // Check NBT backup if not found (for offline owners)
             if (!isTargetSummoned) {
                 var entityData = entity.getPersistentData();
                 if (entityData.contains("lord_of_souls_owner")) {
                     isTargetSummoned = true;
                     try {
                         targetOwnerUUID = entityData.getUUID("lord_of_souls_owner");
                     } catch (Exception e) {
                         // Invalid UUID format, skip
                     }
                 }
             }
             
             // Only skip if it's from the SAME owner
             if (isTargetSummoned && targetOwnerUUID != null && targetOwnerUUID.equals(ownerUUID)) {
                 continue; // Skip this entity
             }
            
            // Calculate distance and find nearest
            double distance = mob.distanceToSqr(entity);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestTarget = entity;
            }
        }
        
        return nearestTarget;
    }
} 