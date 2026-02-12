package net.artur.nacikmod.capability.mana;

import net.artur.nacikmod.NacikMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.MinecraftServer;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.minecraftforge.event.TickEvent;
import net.minecraft.stats.Stats;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class ManaAdvancementTracker {
    
    private static final ResourceLocation TRUE_MAGE_ADVANCEMENT = new ResourceLocation("nacikmod", "true_mage");
    
    /**
     * Отслеживает изменения максимальной маны игрока каждые 20 тиков (1 раз в секунду)
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        // Проверяем всех игроков каждую секунду
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            // Проверяем, есть ли у игрока capability маны
            player.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                int maxMana = mana.getMaxMana();
                
                // Проверяем, что у игрока действительно развита мана (не базовая 100)
                // и что максимальная мана достигла или превысила 10000
                if (maxMana >= 15000) {
                    grantTrueMageAdvancement(player);
                }
            });
        }
    }
    
    /**
     * Выдает достижение "True Mage" игроку
     */
    private static void grantTrueMageAdvancement(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) return;
        
        Advancement advancement = server.getAdvancements().getAdvancement(TRUE_MAGE_ADVANCEMENT);
        if (advancement == null) return;
        
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        
        // Проверяем, не получено ли уже достижение
        if (progress.isDone()) {
            return;
        }
        
        // Выдаем достижение
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}
