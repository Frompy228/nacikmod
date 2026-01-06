package net.artur.nacikmod.event;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.armor.models.DarkSphereModel;
import net.artur.nacikmod.armor.models.LeonidHelmetModel;
import net.artur.nacikmod.client.renderer.DarkSphereRenderer;
import net.artur.nacikmod.client.renderer.HundredSealLayer;
import net.artur.nacikmod.client.renderer.ReleaseAuraRenderer;
import net.artur.nacikmod.client.renderer.LastMagicAuraRenderer;
import net.artur.nacikmod.entity.client.*;
import net.artur.nacikmod.entity.client.SuppressingGateRenderer;
import net.artur.nacikmod.entity.client.ShamakRenderer;
import net.artur.nacikmod.entity.client.SuppressingGateModel;
import net.artur.nacikmod.gui.TimeStopOverlay;
import net.artur.nacikmod.item.MagicCrystal;
import net.artur.nacikmod.registry.ModEntities;
import net.artur.nacikmod.registry.ModItems;
import net.artur.nacikmod.util.ModItemProperties;
import net.artur.nacikmod.gui.EnchantmentLimitTableScreen;
import net.artur.nacikmod.registry.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.renderer.item.ItemPropertyFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;


@Mod.EventBusSubscriber(modid = NacikMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModEventBusClientEvents {

    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ModModelLayers.LANSER_LAYER, LanserModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.LANSER_OUTER_LAYER, () -> LanserModel.createBodyLayer(new CubeDeformation(0.2F)));
        event.registerLayerDefinition(ModModelLayers.ARCHER_OUTER_LAYER, () -> ArcherModel.createBodyLayer(new CubeDeformation(0.2F)));
        event.registerLayerDefinition(ModModelLayers.BERSERKER_OUTER_LAYER, () -> BerserkerModel.createBodyLayer(new CubeDeformation(0.2F)));
        event.registerLayerDefinition(ModModelLayers.RED_BERSERKER_OUTER_LAYER, () -> BerserkerModel.createBodyLayer(new CubeDeformation(0.2F)));
        event.registerLayerDefinition(ModModelLayers.ASSASSIN_OUTER_LAYER, () -> AssassinModel.createBodyLayer(new CubeDeformation(0.2F)));
        event.registerLayerDefinition(ModModelLayers.LEONID_LAYER, LeonidModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.SPARTAN_LAYER, SpartanModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.BERSERKER_LAYER, BerserkerModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.RED_BERSERKER_LAYER, BerserkerModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.ARCHER_LAYER, ArcherModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.ASSASSIN_LAYER, AssassinModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.MYSTERIOUS_TRADER_LAYER, MysteriousTraderModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.MYSTERIOUS_TRADER_BATTLE_CLONE_LAYER, MysteriousTraderBattleCloneModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.BLOOD_WARRIOR_LAYER, BloodWarriorModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.INQUISITOR_LAYER, InquisitorModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.INQUISITOR_OUTER_LAYER, () -> InquisitorModel.createBodyLayer(new CubeDeformation(0.2F)));
        event.registerLayerDefinition(FireArrowModel.LAYER_LOCATION, FireArrowModel::createBodyLayer);
        event.registerLayerDefinition(ProjectileManaSwordModel.LAYER_LOCATION, ProjectileManaSwordModel::createBodyLayer);
        event.registerLayerDefinition(BloodShootProjectileModel.LAYER_LOCATION, BloodShootProjectileModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.LEONID_HELMET_LAYER, () -> LeonidHelmetModel.createBodyLayer(new CubeDeformation(0.0F)));
        event.registerLayerDefinition(ModModelLayers.DARK_SPHERE_LAYER, DarkSphereModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.MANA_ARROW_LAYER, ManaArrowModel::createBodyLayer);
        event.registerLayerDefinition(IceSpikeModel.LAYER_LOCATION, IceSpikeModel::createBodyLayer);
        event.registerLayerDefinition(SlashProjectileModel.LAYER_LOCATION, SlashProjectileModel::createBodyLayer);
        event.registerLayerDefinition(DoubleSlashProjectileModel.LAYER_LOCATION, DoubleSlashProjectileModel::createBodyLayer);
        event.registerLayerDefinition(TripleSlashProjectileModel.LAYER_LOCATION, TripleSlashProjectileModel::createBodyLayer);
        event.registerLayerDefinition(SuppressingGateModel.LAYER_LOCATION, SuppressingGateModel::createBodyLayer);
        event.registerLayerDefinition(FireWallModel.LAYER_LOCATION, FireWallModel::createBodyLayer);
        event.registerLayerDefinition(ModModelLayers.GRAAL_LAYER, GraalModel::createBodyLayer);
        event.registerLayerDefinition(FireballModel.LAYER_LOCATION, FireballModel::createBodyLayer);
        event.registerLayerDefinition(CrossModel.LAYER_LOCATION, CrossModel::createBodyLayer);
        event.registerLayerDefinition(net.artur.nacikmod.client.renderer.eye.EyeModel.LAYER_LOCATION, net.artur.nacikmod.client.renderer.eye.EyeModel::createBodyLayer);
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
            renderer.addLayer(new DomainLayer(renderer));
            renderer.addLayer(new SimpleDomainLayer(renderer));
            }
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ModItemProperties.addCustomItemProperties();
            ModEventBusClientEvents.registerItemProperties();
            ModEventBusClientEvents.registerShieldProperties();
            EntityRenderers.register(ModEntities.MANA_SWORD_PROJECTILE.get(), ManaSwordProjectileRenderer::new);
            EntityRenderers.register(ModEntities.BLOOD_SHOOT_PROJECTILE.get(), BloodShootProjectileRenderer::new);
            EntityRenderers.register(ModEntities.LANSER.get(), LanserRender::new);
            EntityRenderers.register(ModEntities.LEONID.get(), LeonidRender::new);
            EntityRenderers.register(ModEntities.SPARTAN.get(), SpartanRender::new);
            EntityRenderers.register(ModEntities.BERSERK.get(), BerserkerRender::new);
            EntityRenderers.register(ModEntities.RED_BERSERK.get(), RedBerserkerRender::new);
            EntityRenderers.register(ModEntities.ARCHER.get(), ArcherRender::new);
            EntityRenderers.register(ModEntities.ASSASSIN.get(), AssassinRender::new);
            EntityRenderers.register(ModEntities.MYSTERIOUS_TRADER.get(), MysteriousTraderRender::new);
            EntityRenderers.register(ModEntities.MYSTERIOUS_TRADER_BATTLE_CLONE.get(), MysteriousTraderBattleCloneRender::new);
            EntityRenderers.register(ModEntities.BLOOD_WARRIOR.get(), BloodWarriorRender::new);
            EntityRenderers.register(ModEntities.INQUISITOR.get(), InquisitorRender::new);
            EntityRenderers.register(ModEntities.FIRE_ARROW.get(), FireArrowRenderer::new);
            EntityRenderers.register(ModEntities.MANA_ARROW.get(), ManaArrowRenderer::new);
            EntityRenderers.register(ModEntities.FIRE_CLOUD.get(), FireCloudRenderer::new);
            EntityRenderers.register(ModEntities.FIRE_HAIL.get(), FireHailRenderer::new);
            EntityRenderers.register(ModEntities.FIRE_WALL.get(), FireWallRenderer::new);
            EntityRenderers.register(ModEntities.FIRE_WALL_DAMAGE_ZONE.get(), NoopRenderer::new);
            EntityRenderers.register(ModEntities.ICE_SPIKE_PROJECTILE.get(), IceSpikeProjectileRenderer::new);
            EntityRenderers.register(ModEntities.SLASH_PROJECTILE.get(), SlashProjectileRenderer::new);
            EntityRenderers.register(ModEntities.DOUBLE_SLASH_PROJECTILE.get(), DoubleSlashProjectileRenderer::new);
            EntityRenderers.register(ModEntities.TRIPLE_SLASH_PROJECTILE.get(), TripleSlashProjectileRenderer::new);
            EntityRenderers.register(ModEntities.SUPPRESSING_GATE.get(), SuppressingGateRenderer::new);
            EntityRenderers.register(ModEntities.SHAMAK.get(), ShamakRenderer::new);
            EntityRenderers.register(ModEntities.GRAIL.get(), GraalRenderer::new);
            EntityRenderers.register(ModEntities.FIREBALL.get(), FireballRenderer::new);
            EntityRenderers.register(ModEntities.CROSS_PROJECTILE.get(), CrossProjectileRenderer::new);
            EntityRenderers.register(ModEntities.FIRE_ANNIHILATION.get(), FireAnnihilationRenderer::new);
            EntityRenderers.register(ModEntities.FIRE_PILLAR.get(), FirePillarRenderer::new);

            // Register Dark Sphere renderer
            CuriosRendererRegistry.register(ModItems.DARK_SPHERE.get(), () -> new DarkSphereRenderer());
            // Установка render layer для FIRE_TRAP
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                net.artur.nacikmod.registry.ModBlocks.FIRE_TRAP.get(),
                net.minecraft.client.renderer.RenderType.cutout()
            );

            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    net.artur.nacikmod.registry.ModBlocks.BLOOD_CIRCLE.get(),
                    net.minecraft.client.renderer.RenderType.cutout() // Или .translucent(), если есть мягкие края
            );
            net.minecraft.client.renderer.ItemBlockRenderTypes.setRenderLayer(
                    net.artur.nacikmod.registry.ModBlocks.BLOOD_CIRCLE_CORNER.get(),
                    net.minecraft.client.renderer.RenderType.cutout()
            );
            
            // Регистрация GUI экранов
            MenuScreens.register(ModMenuTypes.ENCHANTMENT_LIMIT_TABLE_MENU.get(), EnchantmentLimitTableScreen::new);
        });
    }

    public static void registerShieldProperties() {
        ItemPropertyFunction blockFn = (stack, level, entity, seed) ->
                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F;

        ItemProperties.register(
                ModItems.LEONID_SHIELD.get(),
                new ResourceLocation("minecraft:blocking"),
                blockFn
        );
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
            renderer.addLayer(new HundredSealLayer.Vanilla<>(renderer));
            renderer.addLayer(new net.artur.nacikmod.client.renderer.eye.EyeLayer(renderer));
        }
    }

}


