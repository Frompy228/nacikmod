package net.artur.nacikmod.effect;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3f;
import net.minecraft.world.phys.AABB;

public class EffectHiraishinMark extends MobEffect {
    public EffectHiraishinMark() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
    }
}
