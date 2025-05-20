package net.artur.nacikmod.capability.reward;

import net.minecraft.nbt.CompoundTag;

public class PlayerRewards implements IPlayerRewards {
    private boolean hasReceivedSpawnReward;
    private boolean hasReceivedTimeReward;
    private boolean hasReceived24hReward;
    private boolean hasReceivedShinraTenseiReward;

    public PlayerRewards() {
        this.hasReceivedSpawnReward = false;
        this.hasReceivedTimeReward = false;
        this.hasReceived24hReward = false;
        this.hasReceivedShinraTenseiReward = false;
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

    @Override
    public boolean hasReceived24hReward() {
        return hasReceived24hReward;
    }

    @Override
    public void setReceived24hReward(boolean value) {
        this.hasReceived24hReward = value;
    }

    @Override
    public boolean hasReceivedShinraTenseiReward() {
        return hasReceivedShinraTenseiReward;
    }

    @Override
    public void setReceivedShinraTenseiReward(boolean value) {
        this.hasReceivedShinraTenseiReward = value;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putBoolean("hasReceivedSpawnReward", hasReceivedSpawnReward);
        nbt.putBoolean("hasReceivedTimeReward", hasReceivedTimeReward);
        nbt.putBoolean("hasReceived24hReward", hasReceived24hReward);
        nbt.putBoolean("hasReceivedShinraTenseiReward", hasReceivedShinraTenseiReward);
    }

    public void loadNBTData(CompoundTag nbt) {
        hasReceivedSpawnReward = nbt.getBoolean("hasReceivedSpawnReward");
        hasReceivedTimeReward = nbt.getBoolean("hasReceivedTimeReward");
        hasReceived24hReward = nbt.getBoolean("hasReceived24hReward");
        hasReceivedShinraTenseiReward = nbt.getBoolean("hasReceivedShinraTenseiReward");
    }
} 