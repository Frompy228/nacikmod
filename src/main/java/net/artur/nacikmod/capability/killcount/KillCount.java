package net.artur.nacikmod.capability.killcount;

import net.minecraft.nbt.CompoundTag;

public class KillCount implements IKillCount {
    private int slashKills;
    private boolean hasReceivedWorldSlashReward;

    public KillCount() {
        this.slashKills = 0;
        this.hasReceivedWorldSlashReward = false;
    }

    @Override
    public int getSlashKills() {
        return slashKills;
    }

    @Override
    public void setSlashKills(int count) {
        this.slashKills = Math.max(0, count);
    }

    @Override
    public void addSlashKill() {
        this.slashKills++;
    }



    @Override
    public boolean hasReceivedWorldSlashReward() {
        return hasReceivedWorldSlashReward;
    }

    @Override
    public void setReceivedWorldSlashReward(boolean value) {
        this.hasReceivedWorldSlashReward = value;
    }

    public void saveNBTData(CompoundTag nbt) {
        nbt.putInt("SlashKills", slashKills);
        nbt.putBoolean("HasReceivedWorldSlashReward", hasReceivedWorldSlashReward);
    }

    public void loadNBTData(CompoundTag nbt) {
        slashKills = nbt.getInt("SlashKills");
        hasReceivedWorldSlashReward = nbt.getBoolean("HasReceivedWorldSlashReward");
    }
} 