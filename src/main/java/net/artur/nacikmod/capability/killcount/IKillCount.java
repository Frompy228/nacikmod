package net.artur.nacikmod.capability.killcount;

public interface IKillCount {
    int getSlashKills();
    void setSlashKills(int count);
    void addSlashKill();
    
    boolean hasReceivedWorldSlashReward();
    void setReceivedWorldSlashReward(boolean value);
} 