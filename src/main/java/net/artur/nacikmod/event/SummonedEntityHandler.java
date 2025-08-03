package net.artur.nacikmod.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.artur.nacikmod.NacikMod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class SummonedEntityHandler {

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity ? (LivingEntity) event.getSource().getEntity() : null;
        
        if (attacker == null || !attacker.getTags().contains("lord_of_souls_summoned")) {
            return;
        }
        
        // Get the owner UUID from tags
        UUID ownerUUID = null;
        for (String tag : attacker.getTags()) {
            if (tag.startsWith("lord_of_souls_owner:")) {
                try {
                    ownerUUID = UUID.fromString(tag.substring("lord_of_souls_owner:".length()));
                    break;
                } catch (IllegalArgumentException e) {
                    // Invalid UUID format
                }
            }
        }
        
        if (ownerUUID == null) {
            return;
        }
        
        // Prevent damage to the owner
        if (event.getEntity() instanceof Player targetPlayer && targetPlayer.getUUID().equals(ownerUUID)) {
            event.setCanceled(true);
            return;
        }
        
        // Prevent damage to animals and fish
        if (event.getEntity() instanceof Animal) {
            event.setCanceled(true);
            return;
        }
        
        // Check if it's a fish (using class name check)
        String entityClassName = event.getEntity().getClass().getSimpleName().toLowerCase();
        if (entityClassName.contains("fish") || entityClassName.contains("salmon") || 
            entityClassName.contains("cod") || entityClassName.contains("pufferfish") ||
            entityClassName.contains("tropical")) {
            event.setCanceled(true);
            return;
        }
    }
} 