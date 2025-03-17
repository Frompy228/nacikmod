package net.artur.nacikmod.event;


import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.effect.EffectTimeSlow;
import net.artur.nacikmod.entity.client.ModModelLayers;
import net.artur.nacikmod.entity.client.LanserModel;
import net.artur.nacikmod.gui.TimeStopOverlay;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event){
        event.registerLayerDefinition(ModModelLayers.LANSER_LAYER, LanserModel::createBodyLayer);
    }
    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event){
        event.register(KeyBindings.INSTANSE.ability);
    }
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("time_stop_overlay", TimeStopOverlay.OVERLAY);
    }
}
