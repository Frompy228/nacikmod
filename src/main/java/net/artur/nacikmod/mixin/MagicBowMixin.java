package net.artur.nacikmod.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.BowItem;
import net.artur.nacikmod.item.MagicBow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.effect.MobEffects;

@Mixin(LocalPlayer.class)
public class MagicBowMixin {
    
    @Inject(at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z",
            ordinal = 0),
            method = "aiStep()V")
    private void onAiStep(CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        ItemStack heldItem = player.getUseItem();

        if (heldItem.getItem() instanceof MagicBow) {
            // Предотвращаем замедление, умножая на 5 (1/0.2) чтобы компенсировать замедление
            player.input.leftImpulse *= 5.0F;
            player.input.forwardImpulse *= 5.0F;
        }
    }

    @Inject(at = @At("HEAD"),
            method = "canStartSprinting()Z",
            cancellable = true)
    private void onCanStartSprinting(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        ItemStack heldItem = player.getUseItem();

        if (heldItem.getItem() instanceof MagicBow) {
            // Разрешаем начать бег при использовании Magic Bow
            boolean canSprint = !player.isSprinting() &&
                              !player.hasEffect(MobEffects.BLINDNESS) &&
                              !player.isFallFlying() &&
                              player.getFoodData().getFoodLevel() > 6.0F &&
                              player.input.forwardImpulse >= 0.4F;

            cir.setReturnValue(canSprint);
        }
    }
} 