package net.artur.nacikmod.capability.cooldowns;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class CooldownsEvents {
    
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(CooldownsProvider.COOLDOWNS_CAPABILITY).isPresent()) {
                event.addCapability(new ResourceLocation(NacikMod.MOD_ID, "cooldowns"), new CooldownsProvider());
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        
        event.getOriginal().getCapability(CooldownsProvider.COOLDOWNS_CAPABILITY).ifPresent(oldStore -> {
            event.getEntity().getCapability(CooldownsProvider.COOLDOWNS_CAPABILITY).ifPresent(newStore -> {
                newStore.deserializeNBT(oldStore.serializeNBT());
            });
        });
    }
    
    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ICooldowns.class);
    }
} 