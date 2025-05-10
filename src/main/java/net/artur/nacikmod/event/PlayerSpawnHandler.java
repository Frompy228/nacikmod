package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.reward.IPlayerRewards;
import net.artur.nacikmod.capability.reward.PlayerRewardsProvider;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlayerSpawnHandler {
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer)) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.getEntity();
        player.getCapability(PlayerRewardsProvider.PLAYER_REWARDS_CAPABILITY).ifPresent(rewards -> {
            if (!rewards.hasReceivedSpawnReward()) {
                // Выдаем предметы
                player.addItem(new ItemStack(ModItems.MAGIC_HEALING.get())); // Magic healing
                player.addItem(new ItemStack(ModItems.RELEASE.get())); // Release

                rewards.setReceivedSpawnReward(true);
            }
        });
    }
}