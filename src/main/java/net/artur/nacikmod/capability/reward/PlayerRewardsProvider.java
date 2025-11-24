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
        tag.putBoolean("1hReward", rewards.hasReceived1hReward());
        tag.putBoolean("2hReward", rewards.hasReceived2hReward());
        tag.putBoolean("2h15mReward", rewards.hasReceived2h15mReward());
        tag.putBoolean("GrailReward", rewards.hasUsedGrailReward());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        rewards.setReceivedSpawnReward(tag.getBoolean("SpawnReward"));
        rewards.setReceivedTimeReward(tag.getBoolean("TimeReward"));
        rewards.setReceived24hReward(tag.getBoolean("24hReward"));
        rewards.setReceivedShinraTenseiReward(tag.getBoolean("ShinraTenseiReward"));
        rewards.setReceived1hReward(tag.getBoolean("1hReward"));
        rewards.setReceived2hReward(tag.getBoolean("2hReward"));
        rewards.setReceived2h15mReward(tag.getBoolean("2h15mReward"));
        rewards.setUsedGrailReward(tag.getBoolean("GrailReward"));
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
            newRewards.setReceived1hReward(oldRewards.hasReceived1hReward());
            newRewards.setReceived2hReward(oldRewards.hasReceived2hReward());
            newRewards.setReceived2h15mReward(oldRewards.hasReceived2h15mReward());
            newRewards.setUsedGrailReward(oldRewards.hasUsedGrailReward());
        }));
    }
} 