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
    private static final int REQUIRED_2H_PLAY_TIME = 7200; // 2 часа в секундах
    private static final int REQUIRED_2H15M_PLAY_TIME = 8100; // 2 часа 15 минут в секундах
    private static final int REQUIRED_5K_MANA = 5000; // 5к максимальной маны
    private static final int REQUIRED_15K_MANA = 15000; // 15к максимальной маны

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
                // Создаем предмет награды
                ItemStack magicWeaponsItem = new ItemStack(ModItems.MAGIC_WEAPONS.get());
                
                // Помечаем, что игрок получил награду
                rewards.setReceivedTimeReward(true);
                
                // Пытаемся добавить предмет в инвентарь
                boolean itemAdded = player.getInventory().add(magicWeaponsItem);
                
                // Отправляем сообщение игроку
                player.sendSystemMessage(Component.literal("You've learned something new!"));
                
                // Если инвентарь полон, выбрасываем предмет рядом с игроком
                if (!itemAdded) {
                    player.drop(magicWeaponsItem, false);
                }
            }

            // Проверяем награду за 1 час
            if (!rewards.hasReceived1hReward() && playTimeSeconds >= REQUIRED_1H_PLAY_TIME) {
                // Создаем предмет награды
                ItemStack fireFlowerItem = new ItemStack(ModItems.FIRE_FLOWER.get());
                
                // Помечаем, что игрок получил награду
                rewards.setReceived1hReward(true);
                
                // Пытаемся добавить предмет в инвентарь
                boolean itemAdded = player.getInventory().add(fireFlowerItem);
                
                // Отправляем сообщение игроку
                player.sendSystemMessage(Component.literal("You've learned something new!"));
                
                // Если инвентарь полон, выбрасываем предмет рядом с игроком
                if (!itemAdded) {
                    player.drop(fireFlowerItem, false);
                }
            }

            // Проверяем награду за 24 часа
            if (!rewards.hasReceived24hReward() && playTimeSeconds >= REQUIRED_24H_PLAY_TIME) {
                // Помечаем, что игрок получил награду
                rewards.setReceived24hReward(true);

                // Отправляем сообщение игроку
                player.sendSystemMessage(Component.literal("Your mana regeneration has been permanently increased by +2!"));
            }

            // Проверяем награду за 2 часа
            if (!rewards.hasReceived2hReward() && playTimeSeconds >= REQUIRED_2H_PLAY_TIME) {
                // Создаем предметы наград
                ItemStack icePrisonItem = new ItemStack(ModItems.ICE_PRISON.get());
                ItemStack magicCircuitItem = new ItemStack(ModItems.MAGIC_CIRCUIT.get(), 2);
                
                // Помечаем, что игрок получил награду
                rewards.setReceived2hReward(true);
                
                // Пытаемся добавить предметы в инвентарь
                boolean icePrisonAdded = player.getInventory().add(icePrisonItem);
                boolean magicCircuitAdded = player.getInventory().add(magicCircuitItem);
                
                // Отправляем сообщение игроку
                player.sendSystemMessage(Component.literal("You've learned something new!"));
                
                // Если инвентарь полон, выбрасываем предметы рядом с игроком
                if (!icePrisonAdded) {
                    player.drop(icePrisonItem, false);
                }
                
                if (!magicCircuitAdded) {
                    player.drop(magicCircuitItem, false);
                }
            }

            // Проверяем награду за 2 часа 15 минут
            if (!rewards.hasReceived2h15mReward() && playTimeSeconds >= REQUIRED_2H15M_PLAY_TIME) {
                // Создаем предметы наград
                ItemStack earthStepItem = new ItemStack(ModItems.EARTH_STEP.get());
                ItemStack magicCircuitItem = new ItemStack(ModItems.MAGIC_CIRCUIT.get(), 2);
                
                // Помечаем, что игрок получил награду
                rewards.setReceived2h15mReward(true);
                
                // Пытаемся добавить предметы в инвентарь
                boolean earthStepAdded = player.getInventory().add(earthStepItem);
                boolean magicCircuitAdded = player.getInventory().add(magicCircuitItem);
                
                // Отправляем сообщение игроку
                player.sendSystemMessage(Component.literal("You've learned something new!"));
                
                // Если инвентарь полон, выбрасываем предметы рядом с игроком
                if (!earthStepAdded) {
                    player.drop(earthStepItem, false);
                }
                
                if (!magicCircuitAdded) {
                    player.drop(magicCircuitItem, false);
                }
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
                            // Создаем предмет награды
                            ItemStack shinraTenseiItem = new ItemStack(ModItems.SHINRA_TENSEI.get());
                            
                            // Помечаем, что игрок получил награду
                            rewards.setReceivedShinraTenseiReward(true);
                            
                            // Пытаемся добавить предмет в инвентарь
                            boolean itemAdded = player.getInventory().add(shinraTenseiItem);
                            
                            // Отправляем сообщение игроку
                            player.sendSystemMessage(Component.literal("You've mastered the Shinra Tensei technique!"));
                            
                            // Если инвентарь полон, выбрасываем предмет рядом с игроком
                            if (!itemAdded) {
                                player.drop(shinraTenseiItem, false);
                            }
                        }
                    });
                }
            }

            // Проверяем награду за достижение 5к максимальной маны
            if (!rewards.hasReceived5kManaReward()) {
                player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                    if (mana.getMaxMana() >= REQUIRED_5K_MANA) {
                        // Помечаем, что игрок получил награду
                        rewards.setReceived5kManaReward(true);
                        
                        // Отправляем сообщение игроку
                        player.sendSystemMessage(Component.literal("Your mana regeneration has been permanently increased by +1!"));
                    }
                });
            }

            // Проверяем награду за достижение 15к максимальной маны
            if (!rewards.hasReceived15kManaReward()) {
                player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                    if (mana.getMaxMana() >= REQUIRED_15K_MANA) {
                        // Помечаем, что игрок получил награду
                        rewards.setReceived15kManaReward(true);
                        
                        // Отправляем сообщение игроку
                        // Модификатор BONUS_ARMOR будет применен автоматически в ManaRegenerationHandler
                        player.sendSystemMessage(Component.literal("Your mana regeneration has been permanently increased by +1 and you gained +1 BONUS_ARMOR!"));
                    }
                });
            }
        });
    }
}