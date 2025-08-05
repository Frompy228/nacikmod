package net.artur.nacikmod.capability.lord_of_souls_summoned_entities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class LordOfSoulsSummonedEntity implements ILordOfSoulsSummonedEntity {
    
    private final Map<UUID, UUID> entityToOwnerMap = new HashMap<>(); // entityUUID -> ownerUUID
    private final Map<UUID, Set<UUID>> ownerToEntitiesMap = new HashMap<>(); // ownerUUID -> Set<entityUUID>
    
    @Override
    public void addSummonedEntity(UUID entityUUID, UUID ownerUUID) {
        entityToOwnerMap.put(entityUUID, ownerUUID);
        
        // Add to owner's entity list
        ownerToEntitiesMap.computeIfAbsent(ownerUUID, k -> new HashSet<>()).add(entityUUID);
    }
    
    @Override
    public void removeSummonedEntity(UUID entityUUID) {
        UUID ownerUUID = entityToOwnerMap.remove(entityUUID);
        if (ownerUUID != null) {
            Set<UUID> ownerEntities = ownerToEntitiesMap.get(ownerUUID);
            if (ownerEntities != null) {
                ownerEntities.remove(entityUUID);
                if (ownerEntities.isEmpty()) {
                    ownerToEntitiesMap.remove(ownerUUID);
                }
            }
        }
    }
    
    @Override
    public Set<UUID> getTrackedEntities() {
        return new HashSet<>(entityToOwnerMap.keySet());
    }
    
    @Override
    public UUID getOwnerUUID(UUID entityUUID) {
        return entityToOwnerMap.get(entityUUID);
    }
    
    @Override
    public boolean isTracked(UUID entityUUID) {
        return entityToOwnerMap.containsKey(entityUUID);
    }
    
    @Override
    public void clear() {
        entityToOwnerMap.clear();
        ownerToEntitiesMap.clear();
    }
    
    @Override
    public Set<UUID> getEntitiesByOwner(UUID ownerUUID) {
        Set<UUID> entities = ownerToEntitiesMap.get(ownerUUID);
        return entities != null ? new HashSet<>(entities) : new HashSet<>();
    }
} 