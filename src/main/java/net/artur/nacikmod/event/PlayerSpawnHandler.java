package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class PlayerSpawnHandler {
    private static final String RECEIVED_MAGIC_HEALING_TAG = "nacikmod:received_magic_healing";
    
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            CompoundTag persistentData = player.getPersistentData();
            
            // Check if player has ever received the item
            if (!persistentData.getBoolean(RECEIVED_MAGIC_HEALING_TAG)) {
                // Give the item
                ItemStack magicHealing = new ItemStack(ModItems.MAGIC_HEALING.get());
                player.getInventory().add(magicHealing);
                
                // Mark that player has received the item
                persistentData.putBoolean(RECEIVED_MAGIC_HEALING_TAG, true);
            }
        }
    }
} 