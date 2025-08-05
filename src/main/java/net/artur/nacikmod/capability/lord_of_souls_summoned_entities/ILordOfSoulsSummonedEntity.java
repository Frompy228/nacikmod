package net.artur.nacikmod.capability.lord_of_souls_summoned_entities;

import java.util.Set;
import java.util.UUID;

public interface ILordOfSoulsSummonedEntity {
    
    /**
     * Add a summoned entity to the tracking list
     */
    void addSummonedEntity(UUID entityUUID, UUID ownerUUID);
    
    /**
     * Remove a summoned entity from the tracking list
     */
    void removeSummonedEntity(UUID entityUUID);
    
    /**
     * Get all tracked entity UUIDs
     */
    Set<UUID> getTrackedEntities();
    
    /**
     * Get owner UUID for a specific entity
     */
    UUID getOwnerUUID(UUID entityUUID);
    
    /**
     * Check if an entity is tracked
     */
    boolean isTracked(UUID entityUUID);
    
    /**
     * Clear all tracked entities
     */
    void clear();
    
    /**
     * Get all entities owned by a specific player
     */
    Set<UUID> getEntitiesByOwner(UUID ownerUUID);
} 