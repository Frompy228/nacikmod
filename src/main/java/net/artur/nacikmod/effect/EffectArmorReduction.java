package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class EffectArmorReduction extends MobEffect {
    private static final UUID ARMOR_REDUCTION_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000"); // Генерация фиксированного UUID

    public EffectArmorReduction() {
        super(MobEffectCategory.HARMFUL, 0x5b2951); // Цвет эффекта

        // Уменьшение брони на 1 (можно сделать зависимость от уровня эффекта)
        this.addAttributeModifier(Attributes.ARMOR, ARMOR_REDUCTION_ID.toString(),
                -1.0, AttributeModifier.Operation.ADDITION);
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
