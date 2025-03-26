package net.artur.nacikmod.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.artur.nacikmod.item.RingOfTime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;



@OnlyIn(Dist.CLIENT)
public class TimeStopOverlay {
    private static boolean showReadyMessage = false;
    private static int readyMessageTimer = 0;
    private static int lastCooldown = -1;

    public static final IGuiOverlay OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        // Теперь перезарядка обновляется каждый кадр
        int cooldown = RingOfTime.getCooldown(player);

        // Определяем, когда показывать сообщение "Готово!"
        if (lastCooldown > 0 && cooldown == 0) {
            showReadyMessage = true;
            readyMessageTimer = 40; // 2 секунды (40 тиков)
        }

        lastCooldown = cooldown; // Обновляем прошлое значение таймера

        // Определяем текст
        Component text = null;
        if (cooldown > 0) {
            text = Component.literal("Перезарядка: " + (cooldown / 20) + " сек.");
        } else if (showReadyMessage) {
            text = Component.literal("Time Stop готов!");
            readyMessageTimer--;
            if (readyMessageTimer <= 0) showReadyMessage = false;
        }

        // Если нет текста - не рисуем
        if (text == null) return;

        // Координаты с учетом масштаба экрана
        int x = (screenWidth - mc.font.width(text)) / 2; // Центрируем текст
        int y = (int) (screenHeight * 0.8); // Позиция над полосками здоровья

        // Рисуем текст
        RenderSystem.enableBlend();
        guiGraphics.drawString(mc.font, text, x, y, 0xFFFFFF, true);
        RenderSystem.disableBlend();
    };
}
