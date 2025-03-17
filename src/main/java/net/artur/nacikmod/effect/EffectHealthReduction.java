package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

public class EffectHealthReduction extends MobEffect {
    private static final UUID HEALTH_REDUCTION_ID = UUID.fromString("e8c4b0d8-45af-4d8d-917f-bbfdfcbe5a5c");

    public EffectHealthReduction() {
        super(MobEffectCategory.HARMFUL, 0x7E1A1A); // Красный цвет эффекта

        // Уменьшение максимального здоровья в зависимости от уровня эффекта
        this.addAttributeModifier(Attributes.MAX_HEALTH, HEALTH_REDUCTION_ID.toString(),
                -1.0, AttributeModifier.Operation.ADDITION);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Эффект обновляется каждую секунду
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.getHealth() > entity.getMaxHealth()) {
            entity.setHealth(entity.getMaxHealth()); // Гарантируем, что текущее здоровье не превышает новое макс. ХП
        }
    }
}
