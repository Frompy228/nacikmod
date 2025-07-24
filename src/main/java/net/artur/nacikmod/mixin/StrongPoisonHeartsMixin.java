package net.artur.nacikmod.mixin;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.client.gui.Gui;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.gui.Gui$HeartType")
public class StrongPoisonHeartsMixin {
    @Inject(
            method = "forPlayer",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void nacikmod$strongPoisonHearts(Player player, CallbackInfoReturnable<Object> cir) {
        if (player.hasEffect(ModEffects.STRONG_POISON.get())) {
            try {
                Class<?> heartTypeClass = Class.forName("net.minecraft.client.gui.Gui$HeartType");
                Object poisoned = null;
                for (Object constant : heartTypeClass.getEnumConstants()) {
                    if (constant.toString().equals("POISIONED")) {
                        poisoned = constant;
                        break;
                    }
                }
                if (poisoned != null) {
                    cir.setReturnValue(poisoned);
                }
            } catch (Exception e) {
                // optionally log or print error
            }
        }
    }
}