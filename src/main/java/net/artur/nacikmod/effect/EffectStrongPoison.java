package net.artur.nacikmod.effect;

import net.artur.nacikmod.registry.ModDamageTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class EffectStrongPoison extends MobEffect {
    public EffectStrongPoison() {
        super(MobEffectCategory.HARMFUL, 0x4E9331); // Ядовито-зелёный цвет
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = Math.max(10, 25 - (amplifier * 5));
        return (duration % interval) == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity.isAlive()) {
            float damage = 1.0f + (amplifier * 0.5f);

            if (entity.level() instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel) entity.level();
                entity.hurt(ModDamageTypes.strongPoison(serverLevel), damage);
            }
        }
    }

}
