package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.SimpleDomain;
import net.artur.nacikmod.network.AbilityStateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class SimpleDomainAbility {
    public static final Set<UUID> activeSimpleDomainPlayers = new HashSet<>();
    private static final String ACTIVE_TAG = "active";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Проверяем все предметы SimpleDomain в инвентаре
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof SimpleDomain) {
                    // Если предмет помечен как активный, но игрок не в списке активных
                    if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG) && !activeSimpleDomainPlayers.contains(player.getUUID())) {
                        stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                    }
                }
            }
        }
    }

    public static void startSimpleDomain(Player player) {
        // Добавляем игрока в список активных
        activeSimpleDomainPlayers.add(player.getUUID());
        
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof SimpleDomain) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                
                // Отправляем пакет всем игрокам поблизости (включая владельца)
                if (player instanceof ServerPlayer serverPlayer) {
                    AbilityStateManager.syncAbilityState(serverPlayer, "simple_domain", true);
                }

                break;
            }
        }
    }

    public static void stopSimpleDomain(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        // Удаляем игрока из списка активных
        activeSimpleDomainPlayers.remove(player.getUUID());

        // Обновляем состояние всех предметов SimpleDomain в инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof SimpleDomain) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }
        
        // Отправляем пакет всем игрокам поблизости (включая владельца)
        if (player instanceof ServerPlayer serverPlayer) {
            AbilityStateManager.syncAbilityState(serverPlayer, "simple_domain", false);
        }
    }

    public static boolean isSimpleDomainActive(Player player) {
        return activeSimpleDomainPlayers.contains(player.getUUID());
    }
}








