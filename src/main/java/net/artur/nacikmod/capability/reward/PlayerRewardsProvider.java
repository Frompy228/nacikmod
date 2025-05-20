package net.artur.nacikmod.capability.reward;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AutoRegisterCapability
public class PlayerRewardsProvider implements ICapabilityProvider, ICapabilitySerializable<CompoundTag> {
    public static final Capability<IPlayerRewards> PLAYER_REWARDS_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    private final IPlayerRewards rewards = new PlayerRewards();
    private final LazyOptional<IPlayerRewards> optionalRewards = LazyOptional.of(() -> rewards);

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == PLAYER_REWARDS_CAPABILITY ? optionalRewards.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("SpawnReward", rewards.hasReceivedSpawnReward());
        tag.putBoolean("TimeReward", rewards.hasReceivedTimeReward());
        tag.putBoolean("24hReward", rewards.hasReceived24hReward());
        tag.putBoolean("ShinraTenseiReward", rewards.hasReceivedShinraTenseiReward());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        rewards.setReceivedSpawnReward(tag.getBoolean("SpawnReward"));
        rewards.setReceivedTimeReward(tag.getBoolean("TimeReward"));
        rewards.setReceived24hReward(tag.getBoolean("24hReward"));
        rewards.setReceivedShinraTenseiReward(tag.getBoolean("ShinraTenseiReward"));
    }

    // Сохранение наград после смерти
    public static void copyForRespawn(Player oldPlayer, Player newPlayer) {
        oldPlayer.reviveCaps();
        newPlayer.reviveCaps();

        LazyOptional<IPlayerRewards> oldRewardsCap = oldPlayer.getCapability(PLAYER_REWARDS_CAPABILITY);
        LazyOptional<IPlayerRewards> newRewardsCap = newPlayer.getCapability(PLAYER_REWARDS_CAPABILITY);

        oldRewardsCap.ifPresent(oldRewards -> newRewardsCap.ifPresent(newRewards -> {
            newRewards.setReceivedSpawnReward(oldRewards.hasReceivedSpawnReward());
            newRewards.setReceivedTimeReward(oldRewards.hasReceivedTimeReward());
            newRewards.setReceived24hReward(oldRewards.hasReceived24hReward());
            newRewards.setReceivedShinraTenseiReward(oldRewards.hasReceivedShinraTenseiReward());
        }));
    }
} 