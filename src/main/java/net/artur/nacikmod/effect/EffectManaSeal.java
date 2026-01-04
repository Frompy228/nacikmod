package net.artur.nacikmod.effect;

import net.artur.nacikmod.capability.mana.ManaProvider;
import net.artur.nacikmod.registry.ModMessages;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffects;

public class EffectManaSeal extends MobEffect {
    public EffectManaSeal() {
        super(MobEffectCategory.HARMFUL, 0x8B008B); // Темно-фиолетовый цвет
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) return;

        // Проверяем текущее здоровье цели
        float currentHealth = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        
        // Проверяем, упало ли здоровье до 10% или ниже
        if ((currentHealth / maxHealth) <= 0.1F) {
            // Срабатывает логика печати
            applySealEffect(entity);
            
            // Удаляем эффект после срабатывания
            entity.removeEffect(ModEffects.EFFECT_MANA_SEAL.get());
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Проверяем каждый тик
    }

    private static void applySealEffect(LivingEntity entity) {
        // Сжигаем всю ману у цели
        entity.getCapability(ManaProvider.MANA_CAPABILITY).ifPresent(mana -> {
            mana.setMana(0);
            // Синхронизируем ману с клиентом, если это игрок
            if (entity instanceof ServerPlayer serverPlayer) {
                ModMessages.sendManaToClient(serverPlayer, mana.getMana(), mana.getMaxMana());
            }
        });

        // Накладываем медлительность (Slowness) на 100 тиков (5 секунд) с уровнем 10
        entity.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN,
            100,
            10,
            false,
            true,
            true
        ));
    }
}
