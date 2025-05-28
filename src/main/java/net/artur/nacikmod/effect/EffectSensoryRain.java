package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

public class EffectSensoryRain extends MobEffect {
    public EffectSensoryRain() {
        super(MobEffectCategory.BENEFICIAL, 0x00FFFF); // Cyan color for the effect
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Effect updates every tick
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Делаем сущность видимой через стены
        entity.setGlowingTag(true);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        // Убираем подсветку при окончании эффекта
        entity.setGlowingTag(false);
    }
} 