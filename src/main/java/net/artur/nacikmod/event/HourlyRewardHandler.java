package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.reward.IPlayerRewards;
import net.artur.nacikmod.capability.reward.PlayerRewardsProvider;
import net.artur.nacikmod.capability.mana.IMana;
import net.artur.nacikmod.capability.mana.ManaProvider;
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
    private static final int REQUIRED_PLAY_TIME = 2700; // 45 minutes
    private static final int REQUIRED_1H_PLAY_TIME = 3600; // 1 hour in seconds
    private static final int REQUIRED_24H_PLAY_TIME = 86400; // 24 hours in seconds
    private static final int REQUIRED_MAX_MANA = 50000; // Требуемая максимальная мана для Shinra Tensei

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer)) {
            return;
        }

        ServerPlayer player = (ServerPlayer) event.player;

        player.getCapability(PlayerRewardsProvider.PLAYER_REWARDS_CAPABILITY).ifPresent(rewards -> {
            // Получаем статистику времени игры в тиках и конвертируем в секунды
            int playTimeTicks = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));
            int playTimeSeconds = playTimeTicks / 20;

            // Проверяем награду за 45 минут
            if (!rewards.hasReceivedTimeReward() && playTimeSeconds >= REQUIRED_PLAY_TIME) {
                // Выдаем награду
                player.addItem(new ItemStack(ModItems.MAGIC_WEAPONS.get()));

                // Помечаем, что игрок получил награду
                rewards.setReceivedTimeReward(true);

                // Отправляем сообщение игроку
                player.sendSystemMessage(Component.literal("You've learned something new!"));
            }

            // Проверяем награду за 1 час
            if (!rewards.hasReceived1hReward() && playTimeSeconds >= REQUIRED_1H_PLAY_TIME) {
                // Выдаем награду
                player.addItem(new ItemStack(ModItems.FIRE_FLOWER.get()));

                // Помечаем, что игрок получил награду
                rewards.setReceived1hReward(true);

                // Отправляем сообщение игроку
                player.sendSystemMessage(Component.literal("You've learned something new!"));
            }

            // Проверяем награду за 24 часа
            if (!rewards.hasReceived24hReward() && playTimeSeconds >= REQUIRED_24H_PLAY_TIME) {
                // Помечаем, что игрок получил награду
                rewards.setReceived24hReward(true);

                // Отправляем сообщение игроку
                player.sendSystemMessage(Component.literal("Your mana regeneration has been permanently increased by +2!"));
            }

            // Проверяем награду Shinra Tensei
            if (!rewards.hasReceivedShinraTenseiReward() && playTimeSeconds >= REQUIRED_24H_PLAY_TIME) {
                // Проверяем наличие предмета Gravity в инвентаре
                boolean hasGravity = false;
                for (ItemStack stack : player.getInventory().items) {
                    if (stack.getItem() == ModItems.GRAVITY.get()) {
                        hasGravity = true;
                        break;
                    }
                }

                // Если есть Gravity, проверяем максимальную ману
                if (hasGravity) {
                    player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                        if (mana.getMaxMana() >= REQUIRED_MAX_MANA) {
                            // Выдаем Shinra Tensei
                            player.addItem(new ItemStack(ModItems.SHINRA_TENSEI.get()));
                            
                            // Помечаем, что игрок получил награду
                            rewards.setReceivedShinraTenseiReward(true);
                            
                            // Отправляем сообщение игроку
                            player.sendSystemMessage(Component.literal("You've mastered the Shinra Tensei technique!"));
                        }
                    });
                }
            }
        });
    }
}