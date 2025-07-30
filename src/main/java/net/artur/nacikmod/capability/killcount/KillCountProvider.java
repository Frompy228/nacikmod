package net.artur.nacikmod.capability.killcount;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegisterCapability
public class KillCountProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static final Capability<IKillCount> KILL_COUNT_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final IKillCount killCount = new KillCount();
    private final LazyOptional<IKillCount> optionalKillCount = LazyOptional.of(() -> killCount);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == KILL_COUNT_CAPABILITY ? optionalKillCount.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("SlashKills", killCount.getSlashKills());
        tag.putBoolean("HasReceivedWorldSlashReward", killCount.hasReceivedWorldSlashReward());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        killCount.setSlashKills(tag.getInt("SlashKills"));
        killCount.setReceivedWorldSlashReward(tag.getBoolean("HasReceivedWorldSlashReward"));
    }

    // Сохранение данных после смерти
    public static void copyForRespawn(Player oldPlayer, Player newPlayer) {
        oldPlayer.reviveCaps();
        newPlayer.reviveCaps();

        LazyOptional<IKillCount> oldKillCountCap = oldPlayer.getCapability(KILL_COUNT_CAPABILITY);
        LazyOptional<IKillCount> newKillCountCap = newPlayer.getCapability(KILL_COUNT_CAPABILITY);

        oldKillCountCap.ifPresent(oldKillCount -> newKillCountCap.ifPresent(newKillCount -> {
            newKillCount.setSlashKills(oldKillCount.getSlashKills());
            newKillCount.setReceivedWorldSlashReward(oldKillCount.hasReceivedWorldSlashReward());
        }));
    }
} 