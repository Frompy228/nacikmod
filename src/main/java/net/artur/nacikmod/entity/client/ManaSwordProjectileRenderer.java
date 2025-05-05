package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.entity.projectiles.ManaSwordProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class ManaSwordProjectileRenderer extends EntityRenderer<ManaSwordProjectile> {
    private final ProjectileManaSwordModel<ManaSwordProjectile> model;

    public ManaSwordProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new ProjectileManaSwordModel<>(context.bakeLayer(ProjectileManaSwordModel.LAYER_LOCATION));
    }

    @Override
    public void render(ManaSwordProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack,
                      MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        
        // Получаем направление движения
        Vec3 motion = entity.getDeltaMovement();
        double motionX = motion.x;
        double motionY = motion.y;
        double motionZ = motion.z;
        
        // Вычисляем углы поворота на основе направления движения
        float yRot = (float) (Mth.atan2(motionX, motionZ) * (180F / (float)Math.PI));
        float xRot = (float) (Mth.atan2(motionY, Mth.sqrt((float) (motionX * motionX + motionZ * motionZ))) * (180F / (float)Math.PI));
        
        // Поворачиваем модель в направлении движения
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(-xRot));
        
        // Разворачиваем модель на 180 градусов
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F));
        
        // Центрируем модель
        poseStack.translate(0.0F, -0.5F, 0.0F);
        
        // Масштабируем модель
        poseStack.scale(0.5F, 0.5F, 0.5F);
        
        // Рендерим модель
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ManaSwordProjectile entity) {
        return new ResourceLocation("nacikmod", "textures/entity/projectile_mana_sword.png");
    }
} 