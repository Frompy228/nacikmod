package net.artur.nacikmod.client;

import net.minecraft.client.Minecraft;

public class MoonTextureManager {
    private static boolean magicNightActive = false;

    public static void activateMagicNight() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            magicNightActive = true;
            return;
        }
        long time = mc.level.getDayTime() % 24000;
        if (time < 13000 || time > 23000) {
            // Если еще не ночь, пробуем снова через небольшой интервал
            mc.execute(() -> {
                // Запланировать повторную попытку через 2 тика (~0.1 сек)
                mc.execute(() -> activateMagicNight());
            });
        } else {
            magicNightActive = true;
        }
    }

    public static void deactivateMagicNight() {
        magicNightActive = false;
    }

    public static boolean shouldUseCustomMoon() {
        if (!magicNightActive) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return false;
        }
        long time = mc.level.getDayTime() % 24000;
        // Кастомная луна только ночью
        if (time < 13000 || time > 23000) {
            // Как только наступает день — выключаем эффект
            magicNightActive = false;
            return false;
        }
        return true;
    }
} 