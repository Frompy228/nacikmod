package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

public class EffectGodHand extends MobEffect {
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("e8c411-45af-4d8d-917f-bbfdfcbe5a5c1");
    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("e8c411-45af-4d8d-917f-bbfdf222225c1");
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("e8c411-45af-3333-917f-bbfdfcbe5a5c1");

    public EffectGodHand() {
        super(MobEffectCategory.BENEFICIAL, 0x2d0606);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // Эффект не требует периодического действия
    }

    @Override
    public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        // amplifier начинается с 0, но для расчёта уровня эффекта нам нужен (amplifier + 1)
        int level = amplifier + 1;
        // Урон
        entity.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(
                new AttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "GodHand damage boost", 1.0 * level, AttributeModifier.Operation.ADDITION));
        // Скорость атаки
        entity.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(
                new AttributeModifier(ATTACK_SPEED_MODIFIER_ID, "GodHand attack speed boost", 0.4 * level, AttributeModifier.Operation.ADDITION));
        // Скорость передвижения
        entity.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(
                new AttributeModifier(MOVEMENT_SPEED_MODIFIER_ID, "GodHand move speed boost", 0.04 * level, AttributeModifier.Operation.ADDITION));
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        entity.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_DAMAGE_MODIFIER_ID);
        entity.getAttribute(Attributes.ATTACK_SPEED).removeModifier(ATTACK_SPEED_MODIFIER_ID);
        entity.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(MOVEMENT_SPEED_MODIFIER_ID);
    }
}
