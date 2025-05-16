package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class EffectRoar extends MobEffect {
    private static final UUID MOVEMENT_MODIFIER_ID = UUID.fromString("7f0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b1f");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("8f0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b2f");
    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("9f0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b3f");
    private static final UUID GRAVITY_MODIFIER_ID = UUID.fromString("bf0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b5f");

    public EffectRoar() {
        super(MobEffectCategory.HARMFUL, 0x964B00); // Brown color for the effect

        // Completely stop movement
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_MODIFIER_ID.toString(),
                -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        // Prevent attacking
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_MODIFIER_ID.toString(),
                -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        // Remove attack damage
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER_ID.toString(),
                -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
        );

        // Increase gravity
        this.addAttributeModifier(
                ForgeMod.ENTITY_GRAVITY.get(),
                GRAVITY_MODIFIER_ID.toString(),
                15.0D,
                AttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Effect updates every tick
    }

}
