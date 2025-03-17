package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.LanserEntity;
import net.artur.nacikmod.network.ModMessages;
import net.artur.nacikmod.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    // Регистрация атрибутов кастомных мобов
    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.LANSER.get(), LanserEntity.createAttributes().build());
    }

}