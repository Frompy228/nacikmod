package net.artur.nacikmod.capability.mana;


import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber
public class ManaSyncOnDeath {
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) { // Проверяем, что игрок умер, а не вышел
            ManaProvider.copyForRespawn(event.getOriginal(), event.getEntity());
        }
    }
}
