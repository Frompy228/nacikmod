package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.client.FireArrowModel;
import net.artur.nacikmod.entity.client.ModModelLayers;
import net.artur.nacikmod.entity.client.LanserModel;
import net.artur.nacikmod.entity.client.MovementSealLayer;
import net.artur.nacikmod.gui.TimeStopOverlay;
import net.artur.nacikmod.item.MagicCrystal;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;


@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.LANSER_LAYER, LanserModel::createBodyLayer);
        event.registerLayerDefinition(FireArrowModel.LAYER_LOCATION, FireArrowModel::createBodyLayer);
    }


    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.ABILITY_KEY);
    }

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("time_stop_overlay", TimeStopOverlay.OVERLAY);
    }

    @SubscribeEvent
    public static void addCustomLayers(EntityRenderersEvent.AddLayers event) {
        for (String skinType : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skinType);
            if (renderer != null) {
                renderer.addLayer(new MovementSealLayer(renderer));
            }
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(ModEventBusClientEvents::registerItemProperties);
    }

    public static void registerItemProperties() {
        ItemProperties.register(ModItems.MANA_CRYSTAL.get(),
                new ResourceLocation("mana_level"),
                (stack, level, entity, seed) -> {
                    return MagicCrystal.getStoredMana(stack) > 100 ? 1.0F : 0.0F;
                });
    }
}


