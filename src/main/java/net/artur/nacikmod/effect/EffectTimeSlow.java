package net.artur.nacikmod.effect;

import com.min01.tickrateapi.util.TickrateUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.UUID;

public class EffectTimeSlow extends MobEffect {


    // Храним флаг, был ли тикрейт уже уменьшен
    private static final HashMap<UUID, Boolean> tickrateModified = new HashMap<>();

    public EffectTimeSlow() {
        super(MobEffectCategory.HARMFUL, 0x7E142A1A);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        float SLOWED_TICKRATE = Math.max(1, 20.0f - (5 + amplifier * 5f));
        if (!entity.level().isClientSide && entity !=null && entity.isAlive()) {  // Выполняем только на сервере
            UUID entityId = entity.getUUID();

            // Если тикрейт еще не был уменьшен, уменьшаем его на 10
            if (!tickrateModified.getOrDefault(entityId, false) && entity !=null) {
                TickrateUtil.setTickrate(entity, SLOWED_TICKRATE);  // Уменьшаем тикрейт на 10
                tickrateModified.put(entityId, true);  // Отмечаем, что тикрейт уже уменьшен
            }

        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        if(entity !=null && entity.isAlive()) {
            tickrateModified.remove(entity.getUUID());  // Сбрасываем флаг
            // Восстанавливаем тикрейт сущности в её первоначальное состояние
            TickrateUtil.resetTickrate(entity);
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;  // Обновляется каждый тик
    }
}
