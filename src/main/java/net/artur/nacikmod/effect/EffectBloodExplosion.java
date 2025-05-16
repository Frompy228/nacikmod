package net.artur.nacikmod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import org.joml.Vector3f;
import net.minecraft.world.phys.AABB;

public class EffectBloodExplosion extends MobEffect {
    public EffectBloodExplosion() {
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        
        if (!entity.level().isClientSide) {
            // Position slightly above the entity
            double x = entity.getX();
            double y = entity.getY() + 1.0; // 1 block above
            double z = entity.getZ();
            
            // Calculate damage based on amplifier (4.0 base + 2.0 per level)
            float damage = 4.0f + (amplifier * 2.0f);
            
            // First, damage the effect owner
            entity.hurt(entity.damageSources().generic(), damage);
            
            // Create area of effect for damage to nearby entities
            AABB damageArea = new AABB(
                x - 2, y - 2, z - 2,
                x + 2, y + 2, z + 2
            );
            
            // Then damage nearby entities (excluding the owner)
            entity.level().getEntities(entity, damageArea).forEach(target -> {
                if (target instanceof LivingEntity livingTarget && livingTarget != entity) {
                    livingTarget.hurt(entity.damageSources().generic(), damage);
                }
            });
            
            // Add custom red particles
            for (int i = 0; i < 100; i++) {
                double particleX = x + (entity.level().getRandom().nextDouble() - 0.5) * 4;
                double particleY = y + (entity.level().getRandom().nextDouble() - 0.5) * 4;
                double particleZ = z + (entity.level().getRandom().nextDouble() - 0.5) * 4;
                
                // Create red dust particles
                DustParticleOptions redDust = new DustParticleOptions(
                    new Vector3f(0.8f, 0.0f, 0.0f), // Red color
                    1.0f // Size
                );
                
                ((ServerLevel)entity.level()).sendParticles(
                    redDust,
                    particleX, particleY, particleZ,
                    1, // Count
                    0, 0, 0, // Speed
                    0 // Speed multiplier
                );
            }
        }
    }
}
