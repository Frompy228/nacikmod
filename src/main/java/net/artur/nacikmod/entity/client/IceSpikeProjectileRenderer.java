package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.entity.projectiles.IceSpikeProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class IceSpikeProjectileRenderer extends EntityRenderer<IceSpikeProjectile> {
    private final IceSpikeModel<IceSpikeProjectile> model;

    public IceSpikeProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new IceSpikeModel<>(context.bakeLayer(IceSpikeModel.LAYER_LOCATION));
    }

    @Override
    public void render(IceSpikeProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack,
                      MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        Vec3 motion = entity.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        float yRot = (float) (Mth.atan2(motionX, motionZ) * (180F / (float)Math.PI));
        float xRot = (float) (Mth.atan2(motionY, Mth.sqrt((float) (motionX * motionX + motionZ * motionZ))) * (180F / (float)Math.PI));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-xRot));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
        poseStack.translate(0.0F, -0.5F, 0.0F);
        poseStack.scale(1.2F, 1.2F, 1.2F);
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(IceSpikeProjectile entity) {
        return new ResourceLocation("nacikmod", "textures/entity/ice_spike_projectile.png");
    }
} 