package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.Domain;
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
public class DomainAbility {
    public static final Set<UUID> activeDomainPlayers = new HashSet<>();
    private static final String ACTIVE_TAG = "active";

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            // Проверяем все предметы Domain в инвентаре
            for (ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof Domain) {
                    // Если предмет помечен как активный, но игрок не в списке активных
                    if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG) && !activeDomainPlayers.contains(player.getUUID())) {
                        stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
                    }
                }
            }
        }
    }

    public static void startDomain(Player player) {
        // Добавляем игрока в список активных
        activeDomainPlayers.add(player.getUUID());
        
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Domain) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, true);
                
                // Отправляем пакет всем игрокам поблизости (включая владельца)
                if (player instanceof ServerPlayer serverPlayer) {
                    AbilityStateManager.syncAbilityState(serverPlayer, "domain", true);
                }

                break;
            }
        }
    }

    public static void stopDomain(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        // Удаляем игрока из списка активных
        activeDomainPlayers.remove(player.getUUID());

        // Обновляем состояние всех предметов Domain в инвентаре
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof Domain) {
                stack.getOrCreateTag().putBoolean(ACTIVE_TAG, false);
            }
        }
        
        // Отправляем пакет всем игрокам поблизости (включая владельца)
        if (player instanceof ServerPlayer serverPlayer) {
            AbilityStateManager.syncAbilityState(serverPlayer, "domain", false);
        }
    }

    public static boolean isDomainActive(Player player) {
        return activeDomainPlayers.contains(player.getUUID());
    }
}









