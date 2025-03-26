package net.artur.nacikmod.capability.root;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class RootData implements IRootData {
    private BlockPos pendingPos;
    private ResourceKey<Level> pendingDim;

    private BlockPos committedPos;
    private ResourceKey<Level> committedDim;
    private boolean initialized;

    @Override
    public BlockPos getPendingPosition() {
        return pendingPos;
    }

    @Override
    public ResourceKey<Level> getPendingDimension() {
        return pendingDim;
    }

    @Override
    public BlockPos getCommittedPosition() {
        return committedPos;
    }

    @Override
    public ResourceKey<Level> getCommittedDimension() {
        return committedDim;
    }

    @Override
    public void setPendingData(BlockPos pos, ResourceKey<Level> dim) {
        this.pendingPos = pos != null ? pos.immutable() : null;
        this.pendingDim = dim;
    }

    @Override
    public void setCommittedData(BlockPos pos, ResourceKey<Level> dim) {
        this.committedPos = pos != null ? pos.immutable() : null;
        this.committedDim = dim;
        this.initialized = true;
    }

    @Override
    public void commitData() {
        if (pendingPos != null && pendingDim != null) {
            this.committedPos = pendingPos;
            this.committedDim = pendingDim;
            this.initialized = true;
        }
    }

    @Override
    public void forceCommitData() {
        if (pendingPos != null && pendingDim != null) {
            this.committedPos = pendingPos;
            this.committedDim = pendingDim;
            this.initialized = true;
        }
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public void clear() {
        this.pendingPos = null;
        this.pendingDim = null;
        this.committedPos = null;
        this.committedDim = null;
        this.initialized = false;
    }
}