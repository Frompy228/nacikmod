package net.artur.nacikmod.capability.cooldowns;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;

public interface ICooldowns {
    /**
     * Проверяет, находится ли предмет на перезарядке
     */
    boolean isOnCooldown(Item item);
    
    /**
     * Устанавливает перезарядку для предмета
     */
    void setCooldown(Item item, int ticks);
    
    /**
     * Возвращает оставшееся время перезарядки в тиках
     */
    int getCooldownLeft(Item item);
    
    /**
     * Очищает перезарядку для предмета
     */
    void clearCooldown(Item item);
    
    /**
     * Очищает все перезарядки
     */
    void clearAllCooldowns();
    
    /**
     * Сохраняет данные в NBT
     */
    CompoundTag serializeNBT();
    
    /**
     * Загружает данные из NBT
     */
    void deserializeNBT(CompoundTag tag);
} 