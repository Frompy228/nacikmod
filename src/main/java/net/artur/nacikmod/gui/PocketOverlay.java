package net.artur.nacikmod.gui;

import net.artur.nacikmod.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class PocketOverlay implements IGuiOverlay {

    // Константа для HUD, которую мы будем регистрировать
    public static final PocketOverlay HUD_INSTANCE = new PocketOverlay();

    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;

        if (player == null) return;

        // Проверяем, использует ли игрок наш предмет
        if (player.isUsingItem()) {
            ItemStack stack = player.getUseItem();

            if (stack.getItem() == ModItems.POCKET.get()) { // Убедитесь, что ModItems.POCKET называется так у вас

                // Вычисляем прогресс
                int useDuration = player.getTicksUsingItem();
                float maxCharge = 50.0f; // 2.5 секунды * 20 тиков
                float progress = Math.min(useDuration / maxCharge, 1.0f);

                // Настройки отрисовки
                int barWidth = 40;   // Ширина полоски
                int barHeight = 4;   // Высота полоски
                int x = (screenWidth - barWidth) / 2;
                int y = (screenHeight / 2) + 15; // Позиция под прицелом (y + смещение)

                // 1. Рисуем фон (темно-серый)
                // fill(x1, y1, x2, y2, colorARGB)
                guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xFF555555);

                // 2. Рисуем прогресс (фиолетовый, под магию)
                int progressWidth = (int) (barWidth * progress);
                // Цвет 0xFF9933CC (фиолетовый) или 0xFF00FF00 (зеленый)
                guiGraphics.fill(x, y, x + progressWidth, y + barHeight, 0xFF9933CC);
            }
        }
    }
}