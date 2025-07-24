package net.artur.nacikmod.item.ability;

import net.artur.nacikmod.item.CursedSword;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;

public class CursedSwordAbility {
    
    // Константы времени
    private static final int ABILITY_DURATION = 140; // 7 секунд (20 тиков * 7)
    
    // Метод для активации способности
    public static boolean activateAbility(Player player) {
        // Просто добавляем эффект
        player.addEffect(new MobEffectInstance(ModEffects.CURSED_SWORD.get(), ABILITY_DURATION, 0,false,false,false));
        return true;
    }
    
    // Проверка активности способности
    public static boolean isActive(Player player) {
        return player.hasEffect(ModEffects.CURSED_SWORD.get());
    }
    
    // Получение оставшегося времени активности
    public static int getActiveTime(Player player) {
        MobEffectInstance effect = player.getEffect(ModEffects.CURSED_SWORD.get());
        return effect != null ? effect.getDuration() : 0;
    }
    
    // Проверка готовности способности (просто проверяем, нет ли эффекта)
    public static boolean isReady(Player player) {
        return !player.hasEffect(ModEffects.CURSED_SWORD.get());
    }
}
