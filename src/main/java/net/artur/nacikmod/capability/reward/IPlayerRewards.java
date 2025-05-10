package net.artur.nacikmod.capability.reward;

public interface IPlayerRewards {
    boolean hasReceivedSpawnReward();
    void setReceivedSpawnReward(boolean value);
    
    boolean hasReceivedTimeReward();
    void setReceivedTimeReward(boolean value);
} 