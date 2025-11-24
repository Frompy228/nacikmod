package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.projectiles.FireballEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FireballRenderer extends EntityRenderer<FireballEntity> {
    private static final ResourceLocation BASE_TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/fireball/magma.png");
    private static final ResourceLocation[] FIRE_TEXTURES = {
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/fireball/fire_0.png"),
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/fireball/fire_1.png"),
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/fireball/fire_2.png"),
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/fireball/fire_3.png"),
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/fireball/fire_4.png"),
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/fireball/fire_5.png"),
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/fireball/fire_6.png"),
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/fireball/fire_7.png")
    };
    
    private final FireballModel<FireballEntity> model;

    public FireballRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FireballModel<>(context.bakeLayer(FireballModel.LAYER_LOCATION));
    }

    @Override
    public void render(FireballEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Центрируем модель
        poseStack.translate(0.0D, entity.getBoundingBox().getYsize() * 0.5f, 0.0D);

        // Получаем направление движения для правильной ориентации
        Vec3 motion = entity.getDeltaMovement();
        if (motion.lengthSqr() > 0.001) {
            float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (180.0F / Math.PI)) - 90.0F);
            float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (180.0F / Math.PI)) + 90.0F);
            poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
            poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        }

        // Масштабирование (увеличен в 3 раза)
        float scale = 3.0f;
        poseStack.scale(scale, scale, scale);

        // Настройка анимации
        this.model.setupAnim(entity, 0.0F, 0.0F, partialTicks, 0.0F, 0.0F);

        // Рендеринг базовой текстуры (magma) с полной яркостью
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        this.model.renderToBuffer(poseStack, vertexConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        // Рендеринг анимированной текстуры огня поверх
        float f = entity.tickCount + partialTicks;
        VertexConsumer fireConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getFireTextureLocation(entity)));
        poseStack.scale(1.15f, 1.15f, 1.15f);
        this.model.renderOutline(poseStack, fireConsumer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FireballEntity entity) {
        return BASE_TEXTURE;
    }

    public ResourceLocation getFireTextureLocation(FireballEntity entity) {
        int frame = (entity.tickCount) % FIRE_TEXTURES.length;
        return FIRE_TEXTURES[frame];
    }
}

