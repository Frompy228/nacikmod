package net.artur.nacikmod.event;


import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.effect.EffectTimeSlow;
import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)

public class ModEventBusEventsForge {
    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event){
        if(KeyBindings.INSTANSE.ability.isDown()){
            KeyBindings.INSTANSE.ability.consumeClick();
        }
    }
    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide) return; // Игнорируем клиент, работаем только на сервере

        LivingEntity entity = event.getEntity();
        AttributeInstance attribute = entity.getAttribute(ModAttributes.BONUS_ARMOR.get());

        if (attribute != null) {
            double bonusArmor = attribute.getValue(); // Получаем значение атрибута

            double reductionPercentage = bonusArmor * 0.025; // 5% снижения урона за 1 бонусной брони
            reductionPercentage = Math.min(reductionPercentage, 0.9); // Максимум 90% снижения

            float reducedDamage = (float) (event.getAmount() * (1 - reductionPercentage));
            event.setAmount(reducedDamage);
        }
    }


}