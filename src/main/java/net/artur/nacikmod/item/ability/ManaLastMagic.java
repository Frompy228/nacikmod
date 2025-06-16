package net.artur.nacikmod.item.ability;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModEffects;
import net.artur.nacikmod.network.ModMessages;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class ManaLastMagic {
    public static final Set<UUID> activeLastMagicPlayers = new HashSet<>();
    private static final int PACKET_SEND_INTERVAL = 20; // Отправляем пакет каждую секунду

    public static void startLastMagic(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        // Добавляем эффект LastMagic
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            ModEffects.MANA_LAST_MAGIC.get(), 2400, 0, false, false));

        // Добавляем игрока в список активных
        activeLastMagicPlayers.add(player.getUUID());

        // Отправляем пакет всем игрокам поблизости
        ModMessages.sendAbilityStateToNearbyPlayers((ServerPlayer) player,
            ManaRelease.isReleaseActive(player),
            true,
            ManaRelease.getCurrentLevel(player).damage);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.END) return;

        // Проверяем, закончился ли эффект LastMagic
        if (!player.hasEffect(ModEffects.MANA_LAST_MAGIC.get()) && 
            activeLastMagicPlayers.contains(player.getUUID())) {
            stopLastMagic(player);
            return;
        }

        // Если у игрока есть эффект LastMagic, отправляем пакет каждую секунду
        if (player.hasEffect(ModEffects.MANA_LAST_MAGIC.get()) && 
            player.tickCount % PACKET_SEND_INTERVAL == 0) {
            ModMessages.sendAbilityStateToNearbyPlayers(player,
                ManaRelease.isReleaseActive(player),
                true,
                ManaRelease.getCurrentLevel(player).damage);
        }
    }

    public static void stopLastMagic(Player player) {
        if (!(player instanceof ServerPlayer)) return;

        // Удаляем игрока из списка активных
        activeLastMagicPlayers.remove(player.getUUID());

        // Отправляем пакет всем игрокам поблизости
        ModMessages.sendAbilityStateToNearbyPlayers((ServerPlayer) player,
            ManaRelease.isReleaseActive(player),
            false,
            ManaRelease.getCurrentLevel(player).damage);
    }
}
