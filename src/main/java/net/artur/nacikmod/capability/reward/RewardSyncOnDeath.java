package net.artur.nacikmod.capability.reward;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class RewardSyncOnDeath {
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) { // Проверяем, что игрок умер, а не вышел
            PlayerRewardsProvider.copyForRespawn(event.getOriginal(), event.getEntity());
        }
    }
    @SubscribeEvent
    public static void onPlayerCloneWithoutDeath(PlayerEvent.Clone event) {
        // Синхронизируем капабилити при любом клонировании игрока
        PlayerRewardsProvider.copyForRespawn(event.getOriginal(), event.getEntity());
    }
}