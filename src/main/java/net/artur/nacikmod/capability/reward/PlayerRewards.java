package net.artur.nacikmod.capability.reward;

import net.minecraft.nbt.CompoundTag;

public class PlayerRewards implements IPlayerRewards {
    private boolean hasReceivedSpawnReward;
    private boolean hasReceivedTimeReward;

    public PlayerRewards() {
        this.hasReceivedSpawnReward = false;
        this.hasReceivedTimeReward = false;
    }

    @Override
    public boolean hasReceivedSpawnReward() {
        return hasReceivedSpawnReward;
    }

    @Override
    public void setReceivedSpawnReward(boolean value) {
        this.hasReceivedSpawnReward = value;
    }

    @Override
    public boolean hasReceivedTimeReward() {
        return hasReceivedTimeReward;
    }

    @Override
    public void setReceivedTimeReward(boolean value) {
        this.hasReceivedTimeReward = value;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putBoolean("hasReceivedSpawnReward", hasReceivedSpawnReward);
        nbt.putBoolean("hasReceivedTimeReward", hasReceivedTimeReward);
    }

    public void loadNBTData(CompoundTag nbt) {
        hasReceivedSpawnReward = nbt.getBoolean("hasReceivedSpawnReward");
        hasReceivedTimeReward = nbt.getBoolean("hasReceivedTimeReward");
    }
} 