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
}
