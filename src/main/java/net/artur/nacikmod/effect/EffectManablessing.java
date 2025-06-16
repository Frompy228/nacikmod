package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class EffectManablessing extends MobEffect {
    public EffectManablessing() {
        super(MobEffectCategory.BENEFICIAL, 0x8B0000);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean isInstantenous() {
        return false;
    }

    @Override
    public boolean isBeneficial() {
        return true;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}
