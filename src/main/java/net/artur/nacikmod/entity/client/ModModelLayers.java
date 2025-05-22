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
}
