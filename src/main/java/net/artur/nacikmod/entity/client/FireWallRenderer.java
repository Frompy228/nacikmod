package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.projectiles.FireWallEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class FireWallRenderer extends EntityRenderer<FireWallEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/fire_wall.png");
    private final FireWallModel<FireWallEntity> model;

    public FireWallRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FireWallModel<>(context.bakeLayer(FireWallModel.LAYER_LOCATION));
    }

    @Override
    public void render(FireWallEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        poseStack.translate(0.0D, 2.5D, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));

        this.model.setupAnim(entity, 0.0F, 0.0F, partialTicks, 0.0F, 0.0F);
        // Используем RenderType.entityTranslucent для поддержки прозрачности
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FireWallEntity entity) {
        return TEXTURE;
    }
}

