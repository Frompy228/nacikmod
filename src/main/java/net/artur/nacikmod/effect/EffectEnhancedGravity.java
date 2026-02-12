package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.common.ForgeMod;

import java.util.UUID;

public class EffectEnhancedGravity extends MobEffect {
    private static final UUID GRAVITY_MODIFIER_ID = UUID.fromString("cf0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b6f");
    private static final UUID SPEED_MODIFIER_ID = UUID.fromString("cf0b6d12-1b7a-4f3f-a9d3-3f0e9a7a5b7f");
    private static final int DAMAGE_INTERVAL = 20; // 1 second (20 ticks)
    private static final float DAMAGE_AMOUNT = 12.0f;

    public EffectEnhancedGravity() {
        super(MobEffectCategory.HARMFUL, 0x808080); // Gray color for the effect

        // Increase gravity significantly
        this.addAttributeModifier(
                ForgeMod.ENTITY_GRAVITY.get(),
                GRAVITY_MODIFIER_ID.toString(),
                100.0D,
                AttributeModifier.Operation.ADDITION
        );

        // Reduce movement speed
        this.addAttributeModifier(
                Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_ID.toString(),
                -0.7D,
                AttributeModifier.Operation.MULTIPLY_TOTAL
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

            // Наносим урон каждую секунду
            if (entity.tickCount % DAMAGE_INTERVAL == 0) {
                entity.hurt(entity.damageSources().generic(), DAMAGE_AMOUNT);
            }
        }
    }
}
