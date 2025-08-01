package net.artur.nacikmod.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.artur.nacikmod.capability.cooldowns.CooldownsProvider;
import net.artur.nacikmod.capability.cooldowns.ICooldowns;

public class PlayerCooldowns {
    
    /**
     * Проверяет, находится ли предмет на перезарядке у игрока
     */
    public static boolean isOnCooldown(Player player, Item item) {
        return player.getCapability(CooldownsProvider.COOLDOWNS_CAPABILITY)
                .map(cooldowns -> cooldowns.isOnCooldown(item))
                .orElse(false);
    }
    
    /**
     * Устанавливает перезарядку для предмета у игрока
     */
    public static void setCooldown(Player player, Item item, int ticks) {
        player.getCapability(CooldownsProvider.COOLDOWNS_CAPABILITY)
                .ifPresent(cooldowns -> cooldowns.setCooldown(item, ticks));
    }
    
    /**
     * Возвращает оставшееся время перезарядки в тиках
     */
    public static int getCooldownLeft(Player player, Item item) {
        return player.getCapability(CooldownsProvider.COOLDOWNS_CAPABILITY)
                .map(cooldowns -> cooldowns.getCooldownLeft(item))
                .orElse(0);
    }
    
    /**
     * Очищает перезарядку для предмета у игрока
     */
    public static void clearCooldown(Player player, Item item) {
        player.getCapability(CooldownsProvider.COOLDOWNS_CAPABILITY)
                .ifPresent(cooldowns -> cooldowns.clearCooldown(item));
    }
    
    /**
     * Очищает все перезарядки у игрока
     */
    public static void clearAllCooldowns(Player player) {
        player.getCapability(CooldownsProvider.COOLDOWNS_CAPABILITY)
                .ifPresent(ICooldowns::clearAllCooldowns);
    }
} 