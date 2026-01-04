package net.artur.nacikmod.capability.lord_of_souls_summoned_entities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@AutoRegisterCapability
public class LordOfSoulsSummonedEntityProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static final Capability<ILordOfSoulsSummonedEntity> LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final ILordOfSoulsSummonedEntity summonedEntities = new LordOfSoulsSummonedEntity();
    private final LazyOptional<ILordOfSoulsSummonedEntity> optionalSummonedEntities = LazyOptional.of(() -> summonedEntities);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY ? optionalSummonedEntities.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag entitiesList = new ListTag();
        
        for (UUID entityUUID : summonedEntities.getTrackedEntities()) {
            UUID ownerUUID = summonedEntities.getOwnerUUID(entityUUID);
            if (ownerUUID != null) {
                CompoundTag entityTag = new CompoundTag();
                entityTag.putUUID("EntityUUID", entityUUID);
                entityTag.putUUID("OwnerUUID", ownerUUID);
                entitiesList.add(entityTag);
            }
        }
        
        tag.put("SummonedEntities", entitiesList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        summonedEntities.clear();
        
        if (tag.contains("SummonedEntities")) {
            ListTag entitiesList = tag.getList("SummonedEntities", 10); // 10 = CompoundTag
            for (int i = 0; i < entitiesList.size(); i++) {
                CompoundTag entityTag = entitiesList.getCompound(i);
                if (entityTag.contains("EntityUUID") && entityTag.contains("OwnerUUID")) {
                    UUID entityUUID = entityTag.getUUID("EntityUUID");
                    UUID ownerUUID = entityTag.getUUID("OwnerUUID");
                    summonedEntities.addSummonedEntity(entityUUID, ownerUUID);
                }
            }
        }
    }

    // Сохранение данных после смерти игрока
    public static void copyForRespawn(Player oldPlayer, Player newPlayer) {
        oldPlayer.reviveCaps();
        newPlayer.reviveCaps();

        LazyOptional<ILordOfSoulsSummonedEntity> oldCap = oldPlayer.getCapability(LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY);
        LazyOptional<ILordOfSoulsSummonedEntity> newCap = newPlayer.getCapability(LORD_OF_SOULS_SUMMONED_ENTITY_CAPABILITY);

        oldCap.ifPresent(oldSummonedEntities -> newCap.ifPresent(newSummonedEntities -> {
            // Копируем все отслеживаемые сущности
            for (UUID entityUUID : oldSummonedEntities.getTrackedEntities()) {
                UUID ownerUUID = oldSummonedEntities.getOwnerUUID(entityUUID);
                if (ownerUUID != null) {
                    newSummonedEntities.addSummonedEntity(entityUUID, ownerUUID);
                }
            }
        }));
    }
} 