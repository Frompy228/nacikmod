package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.reward.IPlayerRewards;
import net.artur.nacikmod.capability.reward.PlayerRewardsProvider;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HourlyRewardHandler {
    private static final int REQUIRED_PLAY_TIME = 2700;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer)) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.player;

        player.getCapability(PlayerRewardsProvider.PLAYER_REWARDS_CAPABILITY).ifPresent(rewards -> {
            if (!rewards.hasReceivedTimeReward()) {
                // Получаем статистику времени игры в тиках и конвертируем в секунды
                int playTimeTicks = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
                int playTimeSeconds = playTimeTicks / 20;

                // Если игрок играл достаточно времени
                if (playTimeSeconds >= REQUIRED_PLAY_TIME) {
                    // Выдаем награду
                    player.addItem(new ItemStack(ModItems.MAGIC_WEAPONS.get()));

                    // Помечаем, что игрок получил награду
                    rewards.setReceivedTimeReward(true);

                    // Отправляем сообщение игроку
                    player.sendSystemMessage(Component.literal("You've learned something new!"));
                }
            }
        });
    }
}