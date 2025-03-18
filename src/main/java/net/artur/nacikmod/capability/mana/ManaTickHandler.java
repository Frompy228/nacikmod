package net.artur.nacikmod.capability.mana;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID)
public class ManaTickHandler {
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player instanceof ServerPlayer serverPlayer) {
            AttributeInstance manaAttribute = serverPlayer.getAttribute(ModAttributes.MANA.get());
            AttributeInstance maxManaAttribute = serverPlayer.getAttribute(ModAttributes.MAX_MANA.get());

            if (manaAttribute == null || maxManaAttribute == null) return;

            double currentMana = manaAttribute.getBaseValue();
            double currentMaxMana = maxManaAttribute.getBaseValue(); // Учитываем все модификаторы

            // Проверка и коррекция текущей маны
            if (currentMana > currentMaxMana) {
                manaAttribute.setBaseValue(currentMaxMana);
            } else if (currentMana < 0) {
                manaAttribute.setBaseValue(0);
            }

            // Восстановление маны каждые 2 секунды (40 тиков)
            if (serverPlayer.tickCount % 40 == 0) {
                if (currentMana < currentMaxMana) {
                    double newMana = currentMana + 1;
                    manaAttribute.setBaseValue(newMana);
                }
            }
        }
    }
}

