package net.artur.nacikmod.mixin;

import net.artur.nacikmod.registry.ModDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityNacikmodMixin {
    @Inject(method = "actuallyHurt", at = @At("TAIL"))
    private void nacikmod$resetHurtTimeForCustomSources(DamageSource source, float amount, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        String id = source.getMsgId();
        if ("nacikmod:strong_poison".equals(id) || "nacikmod:breaking_body_limit".equals(id)) {
            self.hurtTime = 0;
            self.invulnerableTime = 0;
        }
    }
}
