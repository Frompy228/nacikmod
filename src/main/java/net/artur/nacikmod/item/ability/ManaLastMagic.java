package net.artur.nacikmod.item.ability;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.LastMagic;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class ManaLastMagic {
    private static final Set<UUID> activeLastMagicPlayers = new HashSet<>();
    private static final int DURATION_TICKS = 2400;

    public static void startLastMagic(Player player) {
        // Проверяем, не активен ли Release
        if (ManaRelease.isReleaseActive(player)) {
            if (player instanceof ServerPlayer) {
                player.sendSystemMessage(Component.literal("Cannot use Last Magic while Release is active")
                        .withStyle(ChatFormatting.AQUA));
            }
            return;
        }

        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof LastMagic) {
                // Удаляем предмет сразу после активации
                stack.shrink(1);
                
                // Применяем эффект
                player.addEffect(new MobEffectInstance(ModEffects.MANA_LAST_MAGIC.get(), DURATION_TICKS, 0));
                activeLastMagicPlayers.add(player.getUUID());
                break;
            }
        }
    }

    public static void stopLastMagic(Player player) {
        activeLastMagicPlayers.remove(player.getUUID());
        player.removeEffect(ModEffects.MANA_LAST_MAGIC.get());
    }

    public static boolean isLastMagicActive(Player player) {
        return activeLastMagicPlayers.contains(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!(event.player instanceof ServerPlayer player)) return;
        if (event.phase != TickEvent.Phase.END) return;

        // Если эффект закончился, но игрок все еще в списке активных
        if (!player.hasEffect(ModEffects.MANA_LAST_MAGIC.get()) && activeLastMagicPlayers.contains(player.getUUID())) {
            stopLastMagic(player);
        }

        // Если LastMagic активен, проверяем и отключаем Release
        if (activeLastMagicPlayers.contains(player.getUUID())) {
            // Отключаем Release если он активен
            if (ManaRelease.isReleaseActive(player)) {
                ManaRelease.stopRelease(player);
            }
        }
    }
}
