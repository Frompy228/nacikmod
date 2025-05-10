package net.artur.nacikmod.effect;


import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class EffectLove extends MobEffect {
    private static final UUID LOVE = UUID.fromString("490326ce-9581-4d13-8910-883042df6501");


    public EffectLove() {
        super(MobEffectCategory.HARMFUL,0xff00c9);

        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, LOVE.toString()
                ,-0.03, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, LOVE.toString()
                ,-0.5, AttributeModifier.Operation.ADDITION);
        this.addAttributeModifier(Attributes.ATTACK_DAMAGE, LOVE.toString()
                ,-12, AttributeModifier.Operation.ADDITION);
    }



    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Эффект обновляется каждую секунду
    }
    @Override
    public boolean isBeneficial() {
        return false;
    }
}
