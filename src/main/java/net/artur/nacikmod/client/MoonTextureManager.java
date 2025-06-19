package net.artur.nacikmod.client;

import net.minecraft.client.Minecraft;

public class MoonTextureManager {
    private static boolean magicNightActive = false;

    public static void activateMagicNight() {
        magicNightActive = true;
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
        if (time < 13000 || time > 23000) {
            magicNightActive = false;
            return false;
        }
        return true;
    }
} 