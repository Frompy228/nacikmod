package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.projectiles.CrossProjectile;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CrossProjectileRenderer extends EntityRenderer<CrossProjectile> {
    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/cross.png");
    private static final ResourceLocation OUTLINE_TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/cross_layer.png");
    
    private final CrossModel<CrossProjectile> model;

    public CrossProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CrossModel<>(context.bakeLayer(CrossModel.LAYER_LOCATION));
    }

    @Override
    public void render(CrossProjectile entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Центрируем модель по высоте хитбокса
        poseStack.translate(0.0D, entity.getBbHeight() * 0.5F, 0.0D);

        // Получаем направление движения для правильной ориентации
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() > 0.001) {
            float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (180.0F / Math.PI)) - 90.0F);
            float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (180.0F / Math.PI)) + 90.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        }

        // Настройка анимации
        this.model.setupAnim(entity, 0.0F, 0.0F, partialTicks, 0.0F, 0.0F);

        // Рендеринг базовой текстуры (cross.png) с полной яркостью
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        // Рендеринг внешнего слоя (cross_layer.png) поверх
        VertexConsumer outlineConsumer = buffer.getBuffer(RenderType.entityTranslucent(getOutlineTextureLocation(entity)));
        this.model.renderOutline(poseStack, outlineConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(CrossProjectile entity) {
        return BASE_TEXTURE;
    }

    public ResourceLocation getOutlineTextureLocation(CrossProjectile entity) {
        return OUTLINE_TEXTURE;
    }
}

