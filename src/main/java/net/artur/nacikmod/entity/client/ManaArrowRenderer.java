package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.entity.projectiles.ManaArrowProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ManaArrowRenderer extends EntityRenderer<ManaArrowProjectile> {
    private final ManaArrowModel<ManaArrowProjectile> model;

    public ManaArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ManaArrowModel<>(context.bakeLayer(ManaArrowModel.LAYER_LOCATION));
    }

    @Override
    public void render(ManaArrowProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Get movement direction
        Vec3 motion = entity.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;

        // Calculate rotation angles based on movement direction
        float yRot = (float) (Mth.atan2(motionX, motionZ) * (180F / (float)Math.PI));
        float xRot = (float) (Mth.atan2(motionY, Mth.sqrt((float) (motionX * motionX + motionZ * motionZ))) * (180F / (float)Math.PI));

        // Rotate model in movement direction
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-xRot));

        // Rotate model 180 degrees
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));

        // Center the model
        poseStack.translate(0.0F, -1.1F, 0.0F);

        // Scale the model
        poseStack.scale(1F, 1F, 1F);

        // Setup animation
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);

        // Render the model with original texture colors
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ManaArrowProjectile entity) {
        return new ResourceLocation("nacikmod", "textures/entity/mana_arrow.png");
    }
}
