package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class EffectCursedSword extends MobEffect {
    private static final UUID RESISTANCE_MODIFIER_UUID = UUID.fromString("1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("2b3c4d5e-6f7a-8b9c-0d1e-2f3a4b5c6d7e");
    
    public EffectCursedSword() {
        super(MobEffectCategory.BENEFICIAL, 0x8B0000); // Тёмно-красный цвет
        
        // Добавляем модификаторы атрибутов
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, RESISTANCE_MODIFIER_UUID.toString(),
                1.2, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID.toString(),
                0.09, AttributeModifier.Operation.ADDITION);
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

}
