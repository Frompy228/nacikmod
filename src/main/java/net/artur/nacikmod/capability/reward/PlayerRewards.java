package net.artur.nacikmod.capability.reward;

import net.minecraft.nbt.CompoundTag;

public class PlayerRewards implements IPlayerRewards {
    private boolean hasReceivedSpawnReward;
    private boolean hasReceivedTimeReward;
    private boolean hasReceived24hReward;
    private boolean hasReceivedShinraTenseiReward;
    private boolean hasReceived1hReward;
    private boolean hasReceived2hReward;
    private boolean hasReceived2h15mReward;

    public PlayerRewards() {
        this.hasReceivedSpawnReward = false;
        this.hasReceivedTimeReward = false;
        this.hasReceived24hReward = false;
        this.hasReceivedShinraTenseiReward = false;
        this.hasReceived1hReward = false;
        this.hasReceived2hReward = false;
        this.hasReceived2h15mReward = false;
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

    @Override
    public boolean hasReceived1hReward() {
        return hasReceived1hReward;
    }

    @Override
    public void setReceived1hReward(boolean value) {
        this.hasReceived1hReward = value;
    }

    @Override
    public boolean hasReceived2hReward() {
        return hasReceived2hReward;
    }

    @Override
    public void setReceived2hReward(boolean value) {
        this.hasReceived2hReward = value;
    }

    @Override
    public boolean hasReceived2h15mReward() {
        return hasReceived2h15mReward;
    }

    @Override
    public void setReceived2h15mReward(boolean value) {
        this.hasReceived2h15mReward = value;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putBoolean("hasReceivedSpawnReward", hasReceivedSpawnReward);
        nbt.putBoolean("hasReceivedTimeReward", hasReceivedTimeReward);
        nbt.putBoolean("hasReceived24hReward", hasReceived24hReward);
        nbt.putBoolean("hasReceivedShinraTenseiReward", hasReceivedShinraTenseiReward);
        nbt.putBoolean("hasReceived1hReward", hasReceived1hReward);
        nbt.putBoolean("hasReceived2hReward", hasReceived2hReward);
        nbt.putBoolean("hasReceived2h15mReward", hasReceived2h15mReward);
    }

    public void loadNBTData(CompoundTag nbt) {
        hasReceivedSpawnReward = nbt.getBoolean("hasReceivedSpawnReward");
        hasReceivedTimeReward = nbt.getBoolean("hasReceivedTimeReward");
        hasReceived24hReward = nbt.getBoolean("hasReceived24hReward");
        hasReceivedShinraTenseiReward = nbt.getBoolean("hasReceivedShinraTenseiReward");
        hasReceived1hReward = nbt.getBoolean("hasReceived1hReward");
        hasReceived2hReward = nbt.getBoolean("hasReceived2hReward");
        hasReceived2h15mReward = nbt.getBoolean("hasReceived2h15mReward");
    }
} 