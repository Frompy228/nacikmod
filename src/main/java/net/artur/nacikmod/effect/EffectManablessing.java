package net.artur.nacikmod.effect;

import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class EffectManablessing extends MobEffect {
    private static final UUID MANA_BLESSING = UUID.fromString("464326ce-9581-4d13-8910-883042df6501");
    public EffectManablessing() {
        super(MobEffectCategory.BENEFICIAL, 0x8B0000);

        this.addAttributeModifier(ModAttributes.BONUS_ARMOR.get(), MANA_BLESSING.toString()
                ,2, AttributeModifier.Operation.ADDITION);
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
