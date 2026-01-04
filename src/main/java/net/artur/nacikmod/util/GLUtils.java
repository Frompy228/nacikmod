package net.artur.nacikmod.util;

public class GLUtils {
    // Теперь мы используем современные методы рендера, флаг глубины можно оставить
    public static boolean hasClearedDepth = false;

    public static int getColor(int a, int r, int g, int b) {
        return a << 24 | r << 16 | g << 8 | b;
    }

    public static int getColor(int r, int g, int b) {
        return 255 << 24 | r << 16 | g << 8 | b;
    }
}