package net.artur.nacikmod.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.ai.LordOfSoulsSummonedEntityAI;
import net.artur.nacikmod.capability.lord_of_souls_summoned_entities.LordOfSoulsSummonedEntityProvider;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class LordOfSoulsSummonedEntityHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ? (LivingEntity) event.getSource().getEntity() : null;
        if (attacker == null) {
            return;
        }

        // Check if attacker is tracked using capability or NBT backup
        boolean isTracked = false;
        UUID ownerUUID = null;
        
        // First, try to find owner through capability (if player is online)
        for (Player player : event.getEntity().level().players()) {
            if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                    .map(cap -> cap.isTracked(attacker.getUUID())).orElse(false)) {
                isTracked = true;
                ownerUUID = player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                        .map(cap -> cap.getOwnerUUID(attacker.getUUID())).orElse(null);
                break;
            }
        }
        
        // If not found through capability, check NBT backup (for offline players)
        if (!isTracked) {
            var attackerData = attacker.getPersistentData();
            if (attackerData.contains("lord_of_souls_owner")) {
                try {
                    ownerUUID = attackerData.getUUID("lord_of_souls_owner");
                    isTracked = true;
                } catch (Exception e) {
                    // Invalid UUID format, skip
                }
            }
        }
        
        if (!isTracked || ownerUUID == null) {
            return;
        }

        // Prevent damage to owner
        if (event.getEntity() instanceof Player targetPlayer && targetPlayer.getUUID().equals(ownerUUID)) {
            event.setCanceled(true);
            // Also clear the attacker's target if it's the owner
            if (attacker instanceof Mob mob) {
                mob.setTarget(null);
                // Force AI restoration
                mob.targetSelector.removeAllGoals(goal -> true);
                mob.targetSelector.addGoal(1, new LordOfSoulsSummonedEntityAI(mob, ownerUUID));
            }
            return;
        }

        // Prevent damage to animals and fish
        if (event.getEntity() instanceof Animal || event.getEntity() instanceof WaterAnimal) {
            event.setCanceled(true);
            return;
        }

        // Prevent damage to other summoned entities from the SAME owner
        boolean isTargetSummoned = false;
        UUID targetOwnerUUID = null;
        
        // Check capability first (if owner is online)
        for (Player player : event.getEntity().level().players()) {
            if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                    .map(cap -> cap.isTracked(event.getEntity().getUUID())).orElse(false)) {
                isTargetSummoned = true;
                targetOwnerUUID = player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                        .map(cap -> cap.getOwnerUUID(event.getEntity().getUUID())).orElse(null);
                break;
            }
        }
        
        // Check NBT backup if not found (for offline owners)
        if (!isTargetSummoned) {
            var targetData = event.getEntity().getPersistentData();
            if (targetData.contains("lord_of_souls_owner")) {
                isTargetSummoned = true;
                try {
                    targetOwnerUUID = targetData.getUUID("lord_of_souls_owner");
                } catch (Exception e) {
                    // Invalid UUID format, skip
                }
            }
        }
        
        // Only prevent damage if both entities are from the SAME owner
        if (isTargetSummoned && targetOwnerUUID != null && targetOwnerUUID.equals(ownerUUID)) {
            event.setCanceled(true);
            return;
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Prevent custom death loot and experience from summoned entities
        boolean isTracked = false;
        
        // Check capability first
        for (Player player : event.getEntity().level().players()) {
            if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                    .map(cap -> cap.isTracked(event.getEntity().getUUID())).orElse(false)) {
                isTracked = true;
                break;
            }
        }
        
        // Check NBT backup if not found
        if (!isTracked) {
            var entityData = event.getEntity().getPersistentData();
            if (entityData.contains("lord_of_souls_owner")) {
                isTracked = true;
            }
        }
        
        if (isTracked) {
            // Cancel the death event to prevent custom loot and experience drops
            event.setCanceled(true);
            // Remove the entity without triggering death drops
            event.getEntity().remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        // Prevent loot drops from summoned entities
        boolean isTracked = false;
        
        // Check capability first
        for (Player player : event.getEntity().level().players()) {
            if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                    .map(cap -> cap.isTracked(event.getEntity().getUUID())).orElse(false)) {
                isTracked = true;
                break;
            }
        }
        
        // Check NBT backup if not found
        if (!isTracked) {
            var entityData = event.getEntity().getPersistentData();
            if (entityData.contains("lord_of_souls_owner")) {
                isTracked = true;
            }
        }
        
        if (isTracked) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingExperienceDrop(LivingExperienceDropEvent event) {
        // Prevent experience drops from summoned entities
        boolean isTracked = false;
        
        // Check capability first
        for (Player player : event.getEntity().level().players()) {
            if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                    .map(cap -> cap.isTracked(event.getEntity().getUUID())).orElse(false)) {
                isTracked = true;
                break;
            }
        }
        
        // Check NBT backup if not found
        if (!isTracked) {
            var entityData = event.getEntity().getPersistentData();
            if (entityData.contains("lord_of_souls_owner")) {
                isTracked = true;
            }
        }
        
        if (isTracked) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        // Check if this is a summoned entity that needs AI restoration using capability OR NBT backup
        UUID ownerUUID = null;
        
        // First, try to find owner through capability (if player is online)
        for (Player player : event.getEntity().level().players()) {
            if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                    .map(cap -> cap.isTracked(mob.getUUID())).orElse(false)) {
                ownerUUID = player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                        .map(cap -> cap.getOwnerUUID(mob.getUUID())).orElse(null);
                break;
            }
        }

        // If not found through capability, check NBT backup (for offline players)
        if (ownerUUID == null) {
            var mobData = mob.getPersistentData();
            if (mobData.contains("lord_of_souls_owner")) {
                try {
                    ownerUUID = mobData.getUUID("lord_of_souls_owner");
                } catch (Exception e) {
                    // Invalid UUID format, skip
                }
            }
        }

        if (ownerUUID != null) {
            final UUID finalOwnerUUID = ownerUUID;
            
            // Schedule AI restoration for next tick to ensure proper initialization
            event.getLevel().getServer().tell(new net.minecraft.server.TickTask(1, () -> {
                if (mob.isAlive()) {
                    // Check if still tracked (either through capability or NBT)
                    boolean stillTracked = false;
                    
                    // Check capability first
                    for (Player player : event.getEntity().level().players()) {
                        if (player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                                .map(cap -> cap.isTracked(mob.getUUID())).orElse(false)) {
                            stillTracked = true;
                            break;
                        }
                    }
                    
                    // Check NBT backup if not found
                    if (!stillTracked) {
                        var mobData = mob.getPersistentData();
                        if (mobData.contains("lord_of_souls_owner")) {
                            stillTracked = true;
                        }
                    }
                    
                    if (stillTracked) {
                        // Clear existing target goals and add our custom AI
                        mob.targetSelector.removeAllGoals(goal -> true);
                        mob.targetSelector.addGoal(1, new LordOfSoulsSummonedEntityAI(mob, finalOwnerUUID));
                        
                        // Clear any existing target that might be the owner
                        if (mob.getTarget() instanceof Player targetPlayer && targetPlayer.getUUID().equals(finalOwnerUUID)) {
                            mob.setTarget(null);
                        }
                        
                        // Set custom properties for summoned entities
                        mob.setPersistenceRequired(); // Prevent despawning
                        
                        // Add lifetime tag if not present
                        var mobData = mob.getPersistentData();
                        if (!mobData.contains("lord_of_souls_lifetime")) {
                            mobData.putInt("lord_of_souls_lifetime", 6000); // 5 minutes default (6000 ticks = 5 minutes)
                        }
                    }
                }
            }));
        }
    }

    private static int aiCheckTickCounter = 0;

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        // Check all tracked entities every 20 ticks (1 second)
        aiCheckTickCounter++;
        if (aiCheckTickCounter < 20) return;
        aiCheckTickCounter = 0;

        // Check all players for their tracked entities (capability-based)
        for (Player player : event.getServer().getPlayerList().getPlayers()) {
            player.getCapability(LordOfSoulsSummonedEntityProvider.LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY)
                    .ifPresent(cap -> {
                        for (UUID entityUUID : cap.getTrackedEntities()) {
                            for (var level : event.getServer().getAllLevels()) {
                                Entity entity = level.getEntity(entityUUID);
                                if (entity instanceof Mob mob && mob.isAlive()) {
                                    // Check if mob has our custom AI
                                    boolean hasCustomAI = false;
                                    for (var goal : mob.targetSelector.getAvailableGoals()) {
                                        if (goal.getGoal() instanceof LordOfSoulsSummonedEntityAI) {
                                            hasCustomAI = true;
                                            break;
                                        }
                                    }

                                    // If no custom AI found, restore it
                                    if (!hasCustomAI) {
                                        UUID ownerUUID = cap.getOwnerUUID(entityUUID);
                                        if (ownerUUID != null) {
                                            // Clear existing target goals and add our custom AI
                                            mob.targetSelector.removeAllGoals(goal -> true);
                                            mob.targetSelector.addGoal(1, new LordOfSoulsSummonedEntityAI(mob, ownerUUID));
                                            
                                            // Clear any existing target that might be the owner
                                            if (mob.getTarget() instanceof Player targetPlayer && targetPlayer.getUUID().equals(ownerUUID)) {
                                                mob.setTarget(null);
                                            }
                                        }
                                    }
                                }
                                break; // Found the entity, no need to check other levels
                            }
                        }
                    });
        }

        // Additional check: Find ALL mobs with NBT tag and restore their AI (for offline players)
        for (var level : event.getServer().getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (entity instanceof Mob mob && mob.isAlive()) {
                    var mobData = mob.getPersistentData();
                    if (mobData.contains("lord_of_souls_owner")) {
                        try {
                            UUID ownerUUID = mobData.getUUID("lord_of_souls_owner");
                            
                            // Check if mob has our custom AI
                            boolean hasCustomAI = false;
                            for (var goal : mob.targetSelector.getAvailableGoals()) {
                                if (goal.getGoal() instanceof LordOfSoulsSummonedEntityAI) {
                                    hasCustomAI = true;
                                    break;
                                }
                            }

                            // If no custom AI found, restore it
                            if (!hasCustomAI) {
                                // Clear existing target goals and add our custom AI
                                mob.targetSelector.removeAllGoals(goal -> true);
                                mob.targetSelector.addGoal(1, new LordOfSoulsSummonedEntityAI(mob, ownerUUID));
                                
                                // Clear any existing target that might be the owner
                                if (mob.getTarget() instanceof Player targetPlayer && targetPlayer.getUUID().equals(ownerUUID)) {
                                    mob.setTarget(null);
                                }
                                
                                // Set custom properties for summoned entities
                                mob.setPersistenceRequired(); // Prevent despawning
                            }
                        } catch (Exception e) {
                            // Invalid UUID format, remove the tag
                            mobData.remove("lord_of_souls_owner");
                        }
                    }
                }
            }
        }
    }
} 