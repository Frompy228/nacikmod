package net.artur.nacikmod.mixin;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    /**
     * Makes entities appear glowing when the player has the TRUE_SIGHT effect
     * Similar to the Planar Sight effect from Iron's Spellbooks
     */
    @Inject(method = "shouldEntityAppearGlowing", at = @At(value = "HEAD"), cancellable = true)
    public void changeGlowOutline(Entity pEntity, CallbackInfoReturnable<Boolean> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        
        // Check if player exists and has TRUE_SIGHT effect
        if (minecraft.player != null && 
            minecraft.player.hasEffect(ModEffects.TRUE_SIGHT.get()) && 
            pEntity instanceof LivingEntity) {
            
            // Calculate distance between player and entity
            double distanceY = Math.abs(pEntity.getY() - minecraft.player.getY());
            
            // Make entities glow within 50 blocks vertical distance
            if (distanceY < 50) {
                cir.setReturnValue(true);
            }
        }
    }
}
