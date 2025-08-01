package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.entity.projectiles.SuppressingGate;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SuppressingGateRenderer extends EntityRenderer<SuppressingGate> {
    private final SuppressingGateModel<SuppressingGate> model;

    public SuppressingGateRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SuppressingGateModel<>(context.bakeLayer(SuppressingGateModel.LAYER_LOCATION));
    }

    @Override
    public void render(SuppressingGate entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Center the model properly - adjust based on model size
        poseStack.translate(0.0F, 1.2F, 0.0F); // Adjust Y offset based on model height

        // Rotate the model 180 degrees to flip it upside down
        poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(180.0F));

        // Rotate the model to face the direction the entity is facing
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(entityYaw));

        // Scale the model
        poseStack.scale(1.0F, 1.0F, 1.0F);

        // Setup animation - no rotation to prevent flipping
        this.model.setupAnim(entity, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

        // Render the model
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SuppressingGate entity) {
        return new ResourceLocation("nacikmod", "textures/entity/suppressing_gate.png");
    }
} 