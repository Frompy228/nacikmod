package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.projectiles.FireArrowEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class FireArrowRenderer extends EntityRenderer<FireArrowEntity> {
    private static final ResourceLocation TEXTURE_LOCATION =
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/fire_arrow.png");

    private final FireArrowModel<FireArrowEntity> model;

    public FireArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FireArrowModel<>(context.bakeLayer(FireArrowModel.LAYER_LOCATION));
    }

    @Override
    public void render(FireArrowEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Center the model
        poseStack.translate(0.0D, -0.1D, 0.0D);

        // Get interpolated rotation
        float yRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float xRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());

        // Apply rotations in the correct order
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

        // Scale the model if needed
        poseStack.scale(1.0f, 1.0f, 1.0f);

        // Setup animation
        this.model.setupAnim(entity, 0, 0, entity.tickCount + partialTicks, 0, 0);

        // Render the model
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(TEXTURE_LOCATION));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0f, 1.0f, 1.0f, 1.0f);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FireArrowEntity entity) {
        return TEXTURE_LOCATION;
    }
}
