package net.artur.nacikmod.capability.root;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public interface IRootData {
    // Pending данные (обновляются при каждом применении эффекта)
    BlockPos getPendingPosition();
    ResourceKey<Level> getPendingDimension();
    void setPendingData(BlockPos pos, ResourceKey<Level> dimension);

    // Committed данные (фиксируются при первом применении)
    BlockPos getCommittedPosition();
    ResourceKey<Level> getCommittedDimension();
    void setCommittedData(BlockPos pos, ResourceKey<Level> dimension);

    // Управление состоянием
    void commitData();
    void forceCommitData();
    boolean isInitialized();
    void setInitialized(boolean initialized);
    void clear();
}