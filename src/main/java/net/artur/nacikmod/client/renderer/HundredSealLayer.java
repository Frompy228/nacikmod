package net.artur.nacikmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.NacikMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.artur.nacikmod.item.ability.HundredSealAbility;

public class HundredSealLayer {
    public static final ResourceLocation SEAL_TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/hundred_seal.png");
    public static final ResourceLocation SEAL_SLIM_TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/hundred_seal_slim.png");
    public static final RenderType SEAL = RenderType.entityTranslucent(SEAL_TEXTURE);
    public static final RenderType SEAL_SLIM = RenderType.entityTranslucent(SEAL_SLIM_TEXTURE);

    public static class Vanilla<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {

        public Vanilla(RenderLayerParent<T, M> pRenderer) {
            super(pRenderer);
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int pPackedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
            var seal = getSealType(livingEntity);
            if (seal != SealType.None) {
                // Выбираем текстуру в зависимости от типа скина
                RenderType renderType = SEAL;
                if (livingEntity instanceof AbstractClientPlayer player) {
                    if (player.getModelName().equals("slim")) {
                        renderType = SEAL_SLIM;
                    }
                }
                
                VertexConsumer vertexconsumer = multiBufferSource.getBuffer(renderType);

                float scale = getSealScale(livingEntity);
                poseStack.pushPose();
                poseStack.scale(scale, scale, scale);
                
                // Рендерим руки и голову с максимальной яркостью
                this.getParentModel().rightArm.render(poseStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, seal.r, seal.g, seal.b, seal.alpha);
                this.getParentModel().leftArm.render(poseStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, seal.r, seal.g, seal.b, seal.alpha);
                this.getParentModel().head.render(poseStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, seal.r, seal.g, seal.b, seal.alpha);
                
                poseStack.popPose();
            }
        }
    }

    public static SealType getSealType(LivingEntity entity) {
        // Проверяем активность Hundred Seal
        if (entity instanceof net.minecraft.world.entity.player.Player player) {
            if (HundredSealAbility.isHundredSealActive(player)) {
                return SealType.Hundred;
            }
        }
        return SealType.None;
    }

    public static float getSealScale(LivingEntity entity) {
        return getSealType(entity).scale;
    }

    public enum SealType {
        None(0f, 0f, 0f, 0f, 0f),
        Hundred(3f, 3f, 3f, 1f, 1f); // Очень яркие белые печати для эффекта скорости

        public final float r, g, b, scale, alpha;

        SealType(float r, float g, float b, float scale, float alpha) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.scale = scale;
            this.alpha = alpha;
        }
    }
} 