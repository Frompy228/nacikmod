package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.entity.projectiles.SlashProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SlashProjectileRenderer extends EntityRenderer<SlashProjectile> {
    private final SlashProjectileModel<SlashProjectile> model;

    public SlashProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SlashProjectileModel<>(context.bakeLayer(SlashProjectileModel.LAYER_LOCATION));
    }

    @Override
    public void render(SlashProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack,
                      MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Поворот по направлению движения
        Vec3 motion = entity.getDeltaMovement();
        float yRot = (float) (Mth.atan2(motion.x, motion.z) * (180F / (float)Math.PI));
        float xRot = (float) (Mth.atan2(motion.y, Mth.sqrt((float) (motion.x * motion.x + motion.z * motion.z))) * (180F / (float)Math.PI));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-xRot));
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
        
        // Центрирование и масштабирование модели
        poseStack.translate(0.0F, -1F, 0.0F);
        poseStack.scale(1F, 1F, 1F);

        // Простой рендеринг текстуры
        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 0.038F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SlashProjectile entity) {
        return new ResourceLocation("nacikmod", "textures/entity/slash.png");
    }
} 