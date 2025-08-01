package net.artur.nacikmod.capability.cooldowns;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

public class Cooldowns implements ICooldowns {
    private final Map<Item, Long> cooldowns = new HashMap<>();
    private long lastUpdateTime = 0;

    @Override
    public boolean isOnCooldown(Item item) {
        updateCooldowns();
        return cooldowns.containsKey(item) && cooldowns.get(item) > getCurrentTime();
    }

    @Override
    public void setCooldown(Item item, int ticks) {
        long endTime = getCurrentTime() + ticks;
        cooldowns.put(item, endTime);
    }

    @Override
    public int getCooldownLeft(Item item) {
        updateCooldowns();
        if (!cooldowns.containsKey(item)) return 0;
        
        long left = cooldowns.get(item) - getCurrentTime();
        return (int) Math.max(left, 0);
    }

    @Override
    public void clearCooldown(Item item) {
        cooldowns.remove(item);
    }

    @Override
    public void clearAllCooldowns() {
        cooldowns.clear();
    }

    /**
     * Обновляет перезарядки, удаляя истёкшие
     */
    private void updateCooldowns() {
        long currentTime = getCurrentTime();
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= currentTime);
    }

    /**
     * Возвращает текущее время в тиках
     */
    private long getCurrentTime() {
        // Используем System.currentTimeMillis() для независимости от игрового времени
        return System.currentTimeMillis() / 50; // Примерно 20 тиков в секунду
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag cooldownList = new ListTag();
        
        updateCooldowns();
        for (Map.Entry<Item, Long> entry : cooldowns.entrySet()) {
            CompoundTag cooldownTag = new CompoundTag();
            ResourceLocation itemId = ForgeRegistries.ITEMS.getKey(entry.getKey());
            if (itemId != null) {
                cooldownTag.putString("item", itemId.toString());
                cooldownTag.putLong("endTime", entry.getValue());
                cooldownList.add(cooldownTag);
            }
        }
        
        tag.put("cooldowns", cooldownList);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        cooldowns.clear();
        
        if (tag.contains("cooldowns", Tag.TAG_LIST)) {
            ListTag cooldownList = tag.getList("cooldowns", Tag.TAG_COMPOUND);
            
            for (Tag t : cooldownList) {
                if (t instanceof CompoundTag cooldownTag) {
                    String itemId = cooldownTag.getString("item");
                    long endTime = cooldownTag.getLong("endTime");
                    
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
                    if (item != null && endTime > getCurrentTime()) {
                        cooldowns.put(item, endTime);
                    }
                }
            }
        }
    }
} 