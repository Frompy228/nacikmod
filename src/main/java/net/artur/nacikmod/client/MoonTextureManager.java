package net.artur.nacikmod.client;

import net.minecraft.client.Minecraft;

public class MoonTextureManager {
    private static boolean customMoonEnabled = false;
    private static long customMoonEndTime = 0;

    public static boolean isCustomMoonEnabled() {
        return customMoonEnabled;
    }

    public static long getCustomMoonEndTime() {
        return customMoonEndTime;
    }

    public static void enableCustomMoon(long duration) {
        customMoonEnabled = true;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            customMoonEndTime = minecraft.level.getDayTime() + duration;
        }
    }

    public static void disableCustomMoon() {
        customMoonEnabled = false;
    }

    public static boolean shouldUseCustomMoon() {
        if (!customMoonEnabled) {
            return false;
        }
        
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null && minecraft.level.getDayTime() > customMoonEndTime) {
            customMoonEnabled = false;
            return false;
        }
        
        return true;
    }
} 