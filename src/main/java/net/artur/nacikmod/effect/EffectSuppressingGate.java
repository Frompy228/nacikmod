package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class EffectSuppressingGate extends MobEffect {
    private static final UUID MOVEMENT_MODIFIER_ID = UUID.fromString("bf0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b5f");
    private static final UUID ATTACK_DAMAGE_MODIFIER_ID = UUID.fromString("bf0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b5e");
    private static final UUID ATTACK_SPEED_MODIFIER_ID = UUID.fromString("bf0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b5d");
    private static final UUID GRAVITY_MODIFIER_ID = UUID.fromString("bf0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b5c");
    private static final UUID KNOCKBACK_RESISTANCE_MODIFIER_ID = UUID.fromString("bf0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b5b");

    public EffectSuppressingGate() {
        super(MobEffectCategory.HARMFUL, 0x8B0000); // Dark red color

        // Reduce movement speed by 10
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_MODIFIER_ID.toString(),
                -10.0D,
                AttributeModifier.Operation.ADDITION
        );

        // Reduce attack damage by 5
        this.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER_ID.toString(),
                -20.0D,
                AttributeModifier.Operation.ADDITION
        );

        // Reduce attack speed by 10
        this.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                ATTACK_SPEED_MODIFIER_ID.toString(),
                -10.0D,
                AttributeModifier.Operation.ADDITION
        );

        // Increase gravity by 10
        this.addAttributeModifier(
                ForgeMod.ENTITY_GRAVITY.get(),
                GRAVITY_MODIFIER_ID.toString(),
                10.0D,
                AttributeModifier.Operation.ADDITION
        );

        // Increase knockback resistance by 0.3
        this.addAttributeModifier(
                Attributes.KNOCKBACK_RESISTANCE,
                KNOCKBACK_RESISTANCE_MODIFIER_ID.toString(),
                0.3D,
                AttributeModifier.Operation.ADDITION
        );
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Effect updates every tick
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            // Отменяем только текущий полет, не трогая возможность полета
            if (entity instanceof net.minecraft.world.entity.player.Player player) {
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }

            // Сжигаем 2 ману каждый тик
            entity.getCapability(net.artur.nacikmod.capability.mana.ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
                int currentMana = mana.getMana();
                if (currentMana > 0) {
                    mana.removeMana(2);
                }
            });
        }
    }
}
