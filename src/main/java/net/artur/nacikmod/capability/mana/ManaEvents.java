package net.artur.nacikmod.capability.mana;

import net.artur.nacikmod.entity.MobClass.HeroSouls;
import net.artur.nacikmod.entity.custom.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ManaEvents {
    
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            event.addCapability(new ResourceLocation("nacikmod", "mana"), new ManaProvider());
        }
        if (event.getObject() instanceof HeroSouls) {
            event.addCapability(new ResourceLocation("nacikmod", "mana"), new ManaProvider());
        }
        if (event.getObject() instanceof MysteriousTraderEntity) {
            event.addCapability(new ResourceLocation("nacikmod", "mana"), new ManaProvider());
        }
    }
}
