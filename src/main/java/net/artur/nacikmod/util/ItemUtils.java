package net.artur.nacikmod.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;

public class ItemUtils {
    public static ItemStack findActiveItem(Player player, Class<? extends ITogglableMagicItem> itemClass) {
        // 1. Инвентарь
        for (ItemStack stack : player.getInventory().items) {
            if (isActive(stack, itemClass)) return stack;
        }
        // 2. Вторая рука
        for (ItemStack stack : player.getInventory().offhand) {
            if (isActive(stack, itemClass)) return stack;
        }
        // 3. Курсор
        ItemStack carried = player.containerMenu.getCarried();
        if (isActive(carried, itemClass)) return carried;

        return null;
    }

    private static boolean isActive(ItemStack stack, Class<? extends ITogglableMagicItem> itemClass) {
        if (!stack.isEmpty() && itemClass.isInstance(stack.getItem())) {
            ITogglableMagicItem item = (ITogglableMagicItem) stack.getItem();
            return stack.hasTag() && stack.getTag().getBoolean(item.getActiveTag());
        }
        return false;
    }

    public interface ITogglableMagicItem {
        String getActiveTag(); // Возвращает название тега (например, "active")
        void deactivate(Player player, ItemStack stack); // Что делать при выключении
    }

    /**
     * Безопасно получает клиентского игрока для использования в tooltip.
     * Возвращает null на сервере или если игрок недоступен.
     * Используется в методах appendHoverText.
     */
    @OnlyIn(Dist.CLIENT)
    private static Player getClientPlayerInternal() {
        try {
            if (net.minecraft.client.Minecraft.getInstance().level != null) {
                return net.minecraft.client.Minecraft.getInstance().player;
            }
        } catch (Exception e) {
            // Игнорируем ошибки на сервере
        }
        return null;
    }

    /**
     * Безопасно получает клиентского игрока. Работает только на клиенте.
     * Используется в методах appendHoverText.
     */
    public static Player getClientPlayer(Level level) {
        if (level == null || !level.isClientSide) {
            return null;
        }
        // Безопасное получение клиентского игрока через DistExecutor
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> getClientPlayerInternal());
    }

    /**
     * Безопасно получает клиентского игрока для использования в isFoil.
     * Возвращает null на сервере или если игрок недоступен.
     */
    public static Player getClientPlayerForFoil() {
        // Безопасное получение клиентского игрока через DistExecutor
        return DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> getClientPlayerInternal());
    }
}