package net.artur.nacikmod.capability.reward;

public interface IPlayerRewards {
    boolean hasReceivedSpawnReward();
    void setReceivedSpawnReward(boolean value);
    
    boolean hasReceivedTimeReward();
    void setReceivedTimeReward(boolean value);

    boolean hasReceived24hReward();
    void setReceived24hReward(boolean value);

    boolean hasReceivedShinraTenseiReward();
    void setReceivedShinraTenseiReward(boolean value);
} 