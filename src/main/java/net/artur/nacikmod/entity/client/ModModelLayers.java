package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.NacikMod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ModModelLayers {
    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation LANSER_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID,"lanser_layer"),"main");

    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation FIRE_ARROW_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID, "fire_arrow"), "main");
            
    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation MANA_ARROW_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID, "mana_arrow"), "main");

    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation LEONID_HELMET_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID, "leonid_helmet"), "main");

    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation LEONID_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID, "leonid_layer"), "main");

    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation SPARTAN_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID, "spartan_layer"), "main");

    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation DARK_SPHERE_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID, "dark_sphere"), "main");

    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation BERSERKER_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID, "berserker_layer"), "main");

    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation ARCHER_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID, "archer_layer"), "main");

    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation MYSTERIOUS_TRADER_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID, "mysterious_trader_layer"), "main");

    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation MYSTERIOUS_TRADER_BATTLE_CLONE_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID, "mysterious_trader_battle_clone_layer"), "main");

    @OnlyIn(Dist.CLIENT)
    public static final ModelLayerLocation BLOOD_WARRIOR_LAYER = new ModelLayerLocation(
            new ResourceLocation(NacikMod.MOD_ID, "blood_warrior_layer"), "main");

}
