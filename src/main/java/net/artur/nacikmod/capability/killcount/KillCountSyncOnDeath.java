package net.artur.nacikmod.capability.killcount;

import net.artur.nacikmod.NacikMod;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KillCountSyncOnDeath {
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player oldPlayer = event.getEntity();
        Player newPlayer = event.getEntity();
        
        // Копируем данные kill count от старого игрока к новому
        KillCountProvider.copyForRespawn(oldPlayer, newPlayer);
    }
} 