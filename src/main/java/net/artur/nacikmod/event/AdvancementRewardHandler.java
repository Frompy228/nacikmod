package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AdvancementEvent.AdvancementEarnEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class AdvancementRewardHandler {
    
    @SubscribeEvent
    public static void onAdvancementEarned(AdvancementEarnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        
        String advancementId = event.getAdvancement().getId().toString();
        
        // Проверяем, является ли это нашим достижением
        if (advancementId.equals("nacikmod:kill_hero_souls")) {
            handleHeroSoulsSlayerAdvancement(player, advancementId);
        } else if (advancementId.equals("nacikmod:true_mage")) {
            handleTrueMageAdvancement(player, advancementId);
        }else if (advancementId.equals("nacikmod:warrior")) {
            handleWarriorAdvancement(player, advancementId);
        }
    }
    
    private static void handleHeroSoulsSlayerAdvancement(ServerPlayer player, String advancementId) {
        // Проверяем, была ли уже выдана награда за это достижение
        if (AdvancementRewardManager.isRewardAlreadyGiven(advancementId)) {
            // Награда уже была выдана другому игроку
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Someone has identified you in achieving the goal"
            ));
            return;
        }
        
        // Отмечаем, что награда выдана
        AdvancementRewardManager.markAsRewarded(advancementId);
        
        // Выдаем награду
        ItemStack rewardItem = new ItemStack(ModItems.LORD_OF_SOULS.get());
        
        // Добавляем предмет в инвентарь игрока
        if (player.getInventory().add(rewardItem)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "Congratulations, you were the first to receive the achievement Slayer Hero Souls"
            ));
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "You have received your well-deserved reward"
            ));
            
            // Оповещаем всех игроков на сервере
            for (ServerPlayer serverPlayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                if (serverPlayer != player) {
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Player" + player.getName().getString() +
                        "the first one achieved the achievement Slayer Hero Souls"
                    ));
                }
            }
        } else {
            // Если инвентарь полон, выбрасываем предмет рядом с игроком
            player.drop(rewardItem, false);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                "Congratulations, you were the first to receive the achievement Slayer Hero Souls"
            ));

            for (ServerPlayer serverPlayer : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
                if (serverPlayer != player) {
                    serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                            "Player" + serverPlayer.getName().getString() +
                                    "the first one achieved the achievement Slayer Hero Souls"
                    ));
                }
            }
        }
    }
    
    private static void handleTrueMageAdvancement(ServerPlayer player, String advancementId) {
        // Выдаем статус True Mage
        player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            // Устанавливаем статус "Истинный маг"
            mana.setTrueMage(true);
        });
        
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "Congratulations! You have achieved the True Mage status"
        ));
        player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
            "You gained +2 bonus armor!"
        ));
    }

    private static void handleWarriorAdvancement(ServerPlayer player, String advancementId) {
        // Проверяем, не выдана ли уже награда (чтобы не дублировать)
        if (AdvancementRewardManager.isRewardAlreadyGiven(advancementId)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "You have already received your reward for this achievement."
            ));
            return;
        }

        // Помечаем как выданное
        AdvancementRewardManager.markAsRewarded(advancementId);

        // Создаём награду
        ItemStack rewardItem = new ItemStack(ModItems.SIMPLE_DOMAIN.get());

        // Пытаемся добавить в инвентарь
        if (player.getInventory().add(rewardItem)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "You received: Simple Possession"
            ));
        } else {
            // Если инвентарь полон
            player.drop(rewardItem, false);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "You received: Simple Possession"
            ));
        }
    }
} 