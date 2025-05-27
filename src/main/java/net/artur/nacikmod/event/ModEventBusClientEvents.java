package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.armor.models.DarkSphereModel;
import net.artur.nacikmod.armor.models.LeonidHelmetModel;
import net.artur.nacikmod.client.renderer.DarkSphereRenderer;
import net.artur.nacikmod.client.renderer.LeonidHelmetRenderer;
import net.artur.nacikmod.client.renderer.ReleaseAuraRenderer;
import net.artur.nacikmod.client.renderer.LastMagicAuraRenderer;
import net.artur.nacikmod.entity.client.*;
import net.artur.nacikmod.gui.TimeStopOverlay;
import net.artur.nacikmod.item.MagicCrystal;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.util.ModItemProperties;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.builders.CubeDeformation;
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
import net.minecraft.client.renderer.entity.EntityRenderers;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;


@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.LANSER_LAYER, LanserModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.LEONID_LAYER, LeonidModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SPARTAN_LAYER, SpartanModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.BERSERKER_LAYER, BerserkerModel::createBodyLayer);
        event.registerLayerDefinition(FireArrowModel.LAYER_LOCATION, FireArrowModel::createBodyLayer);
        event.registerLayerDefinition(ProjectileManaSwordModel.LAYER_LOCATION, ProjectileManaSwordModel::createBodyLayer);
        event.registerLayerDefinition(BloodShootProjectileModel.LAYER_LOCATION, BloodShootProjectileModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.LEONID_HELMET_LAYER, () -> LeonidHelmetModel.createBodyLayer(new CubeDeformation(0.0F)));
        event.registerLayerDefinition(ModModelLayers.DARK_SPHERE_LAYER, DarkSphereModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.MANA_ARROW_LAYER, ManaArrowModel::createBodyLayer);
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
        event.enqueueWork(() -> {
            ModItemProperties.addCustomItemProperties();
            ModEventBusClientEvents.registerItemProperties();
            EntityRenderers.register(ModEntities.MANA_SWORD_PROJECTILE.get(), ManaSwordProjectileRenderer::new);
            EntityRenderers.register(ModEntities.BLOOD_SHOOT_PROJECTILE.get(), BloodShootProjectileRenderer::new);
            EntityRenderers.register(ModEntities.LANSER.get(), LanserRender::new);
            EntityRenderers.register(ModEntities.LEONID.get(), LeonidRender::new);
            EntityRenderers.register(ModEntities.SPARTAN.get(), SpartanRender::new);
            EntityRenderers.register(ModEntities.BERSERK.get(), BerserkerRender::new);
            EntityRenderers.register(ModEntities.FIRE_ARROW.get(), FireArrowRenderer::new);
            EntityRenderers.register(ModEntities.MANA_ARROW.get(), ManaArrowRenderer::new);
            
            // Register Dark Sphere renderer
            CuriosRendererRegistry.register(ModItems.DARK_SPHERE.get(), () -> new DarkSphereRenderer());
        });
    }

    public static void registerItemProperties() {
        ItemProperties.register(ModItems.MANA_CRYSTAL.get(),
                new ResourceLocation("mana_level"),
                (stack, level, entity, seed) -> {
                    return MagicCrystal.getStoredMana(stack) > 100 ? 1.0F : 0.0F;
                });
    }

    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.AddLayers event) {
        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            renderer.addLayer(new ReleaseAuraRenderer(renderer));
            renderer.addLayer(new LastMagicAuraRenderer(renderer));
        }
    }
}


