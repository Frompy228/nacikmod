package net.artur.nacikmod.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.artur.nacikmod.entity.client.BerserkerModel;
import net.artur.nacikmod.entity.client.ModModelLayers;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.BerserkerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BerserkerOuterLayer<T extends BerserkerEntity> extends RenderLayer<T, BerserkerModel<T>> {
    private static final ResourceLocation BERSERKER_OUTER_LAYER_LOCATION = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/berserker_outer_layer.png");
    private final BerserkerModel<T> model;

    public BerserkerOuterLayer(RenderLayerParent<T, BerserkerModel<T>> parent, EntityModelSet modelSet) {
        super(parent);
        this.model = new BerserkerModel<>(modelSet.bakeLayer(ModModelLayers.BERSERKER_OUTER_LAYER));
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, 
                      float limbSwing, float limbSwingAmount, float partialTicks, 
                      float ageInTicks, float netHeadYaw, float headPitch) {
        coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, BERSERKER_OUTER_LAYER_LOCATION, 
                                        poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, 
                                        ageInTicks, netHeadYaw, headPitch, partialTicks, 1.0F, 1.0F, 1.0F);
    }
}
