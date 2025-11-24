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

    boolean hasReceived1hReward();
    void setReceived1hReward(boolean value);

    boolean hasReceived2hReward();
    void setReceived2hReward(boolean value);

    boolean hasReceived2h15mReward();
    void setReceived2h15mReward(boolean value);

    boolean hasUsedGrailReward();
    void setUsedGrailReward(boolean value);
} 