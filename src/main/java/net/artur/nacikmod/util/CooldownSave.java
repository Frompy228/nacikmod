package net.artur.nacikmod.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class CooldownSave {
    private static final String COOLDOWN_TAG = "cooldownEnd";

    // Устанавливает кулдаун (в тиках) и сохраняет его в NBT предмета
    public static void setCooldown(ItemStack stack, Level level, int ticks) {
        if (level == null) return;
        long cooldownEnd = level.getGameTime() + ticks;
        stack.getOrCreateTag().putLong(COOLDOWN_TAG, cooldownEnd);
    }

    // Проверяет, есть ли активный кулдаун
    public static boolean isOnCooldown(ItemStack stack, Level level) {
        if (level == null || !stack.hasTag() || !stack.getTag().contains(COOLDOWN_TAG)) return false;
        long cooldownEnd = stack.getTag().getLong(COOLDOWN_TAG);
        return level.getGameTime() < cooldownEnd;
    }

    // Возвращает оставшееся время кулдауна (в тиках)
    public static int getCooldownLeft(ItemStack stack, Level level) {
        if (level == null || !stack.hasTag() || !stack.getTag().contains(COOLDOWN_TAG)) return 0;
        long cooldownEnd = stack.getTag().getLong(COOLDOWN_TAG);
        long left = cooldownEnd - level.getGameTime();
        return (int) Math.max(left, 0);
    }

    // Восстанавливает кулдаун для игрока (например, при входе)
    public static void restoreCooldown(ItemStack stack, Level level, Player player, Item item) {
        int left = getCooldownLeft(stack, level);
        if (left > 0) {
            player.getCooldowns().addCooldown(item, left);
        }
    }
}
