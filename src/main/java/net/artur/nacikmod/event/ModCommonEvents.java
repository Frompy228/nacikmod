package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModAttributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCommonEvents {

    @SubscribeEvent
    public static void onEntityAttributeModification(EntityAttributeModificationEvent event) {
        // Добавляем атрибут BONUS_ARMOR для всех живых существ
        for (EntityType<? extends LivingEntity> entityType : event.getTypes()) {
            event.add(entityType, ModAttributes.BONUS_ARMOR.get());
        }
        // Для игрока добавляем дополнительные атрибуты маны
        event.add(EntityType.PLAYER, ModAttributes.MANA.get());
        event.add(EntityType.PLAYER, ModAttributes.MAX_MANA.get());
    }
}
