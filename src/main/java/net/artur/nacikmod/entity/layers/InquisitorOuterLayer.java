package net.artur.nacikmod.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.client.InquisitorModel;
import net.artur.nacikmod.entity.client.ModModelLayers;
import net.artur.nacikmod.entity.custom.InquisitorEntity;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class InquisitorOuterLayer<T extends InquisitorEntity> extends RenderLayer<T, InquisitorModel<T>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/inquisitor_outer_layer.png");
    private final InquisitorModel<T> model;

    public InquisitorOuterLayer(RenderLayerParent<T, InquisitorModel<T>> parent, EntityModelSet modelSet) {
        super(parent);
        this.model = new InquisitorModel<>(modelSet.bakeLayer(ModModelLayers.INQUISITOR_OUTER_LAYER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, TEXTURE, poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, partialTicks, 1.0F, 1.0F, 1.0F);
    }
}








