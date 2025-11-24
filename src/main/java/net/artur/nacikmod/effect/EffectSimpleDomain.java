package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class EffectSimpleDomain extends MobEffect {
    private static final UUID MOVEMENT_SPEED_MODIFIER_ID = UUID.fromString("a1b2c3d4-5e6f-7a8b-9c0d-e1f2a3b4c5d6");
    private static final UUID ENTITY_REACH_MODIFIER_ID = UUID.fromString("b2c3d4e5-6f7a-8b9c-0d1e-f2a3b4c5d6e7");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("c3d4e5f6-7a8b-9c0d-1e2f-a3b4c5d6e7f8");



    public EffectSimpleDomain() {
        super(MobEffectCategory.BENEFICIAL, 0x8B4513);

        // Уменьшаем скорость движения на 0.01
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_MODIFIER_ID.toString(),
                -0.01D,
                AttributeModifier.Operation.ADDITION
        );

        // Увеличиваем дальность досягаемости сущностей на 0.1
        this.addAttributeModifier(
                ForgeMod.ENTITY_REACH.get(),
                ENTITY_REACH_MODIFIER_ID.toString(),
                0.1D,
                AttributeModifier.Operation.ADDITION
        );

        // Увеличиваем скорость атаки на 1
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_MODIFIER_ID.toString(),
                0.1D,
                AttributeModifier.Operation.ADDITION
        );
    }
}
