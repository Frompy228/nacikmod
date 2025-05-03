package net.artur.nacikmod.effect;

import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class NoRegenerationEffect extends MobEffect {
    public NoRegenerationEffect() {
        super(MobEffectCategory.HARMFUL, 0x5A5A5A); // Серый цвет эффекта
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            FoodData foodStats = player.getFoodData();
            foodStats.setSaturation(0); // Убираем насыщение, чтобы отключить естественную регенерацию
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true; // Эффект действует постоянно
    }

    // Обработчик, который отменяет любое исцеление
    @Mod.EventBusSubscriber
    public static class NoRegenEventHandler {
        @SubscribeEvent
        public static void onLivingHeal(LivingHealEvent event) {
            LivingEntity entity = event.getEntity();
            if (entity.hasEffect(ModEffects.NO_REGEN.get())) {
                event.setCanceled(true); // Полностью блокируем восстановление здоровья
            }
        }
    }
    @Override
    public boolean isBeneficial() {
        return false; // Указываем, что эффект положительный
    }
}
