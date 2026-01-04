package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.network.EyeParticlePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = "nacikmod", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EyeAbility {
    private static final Set<UUID> activeEyePlayers = new HashSet<>();
    private static final String ACTIVE_TAG = "EyesActive";

    public static void toggleEyes(Player player) {
        UUID playerUUID = player.getUUID();
        if (activeEyePlayers.contains(playerUUID)) {
            activeEyePlayers.remove(playerUUID);
        } else {
            activeEyePlayers.add(playerUUID);
        }
    }

    public static boolean hasEyesActive(net.minecraft.world.entity.player.Player player) {
        if (player.level().isClientSide) {
            // На клиенте проверяем через NBT предмета в инвентаре
            for (net.minecraft.world.item.ItemStack stack : player.getInventory().items) {
                if (stack.getItem() instanceof net.artur.nacikmod.item.Eye) {
                    return stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG);
                }
            }
            return false;
        }
        // На сервере используем Set
        return activeEyePlayers.contains(player.getUUID());
    }

    public static void setEyesActive(Player player, boolean active) {
        UUID playerUUID = player.getUUID();
        if (active) {
            activeEyePlayers.add(playerUUID);
        } else {
            activeEyePlayers.remove(playerUUID);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof net.minecraft.server.level.ServerPlayer player)) return;

        // Проверяем наличие предмета Eye в инвентаре
        boolean hasEyeItem = false;
        for (net.minecraft.world.item.ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof net.artur.nacikmod.item.Eye) {
                hasEyeItem = true;
                // Синхронизируем состояние с NBT предмета
                if (stack.hasTag() && stack.getTag().getBoolean(ACTIVE_TAG)) {
                    if (!activeEyePlayers.contains(player.getUUID())) {
                        activeEyePlayers.add(player.getUUID());
                    }
                } else {
                    if (activeEyePlayers.contains(player.getUUID())) {
                        activeEyePlayers.remove(player.getUUID());
                    }
                }
                break;
            }
        }

        // Если предмета нет в инвентаре, отключаем глаза
        if (!hasEyeItem && activeEyePlayers.contains(player.getUUID())) {
            activeEyePlayers.remove(player.getUUID());
        }

        
    }
}
