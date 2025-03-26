package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class MovementSealLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/movement_seal.png");
    private final MovementSealModel<AbstractClientPlayer> model;

    public MovementSealLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        this.model = new MovementSealModel<>(MovementSealModel.createBodyLayer().bakeRoot());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // Проверяем, есть ли у игрока эффект
        MobEffectInstance effect = player.getEffect(ModEffects.ROOT.get());
        if (effect != null) {
            poseStack.pushPose();

            // Позиционируем модель (изменяй при необходимости)
            poseStack.translate(0.0D, 0D, 0.0D);
            poseStack.scale(1.0F, 1.0F, 1.0F);

            var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
            model.renderToBuffer(poseStack, vertexConsumer, packedLight,
                    LivingEntityRenderer.getOverlayCoords(player, 0.0F),
                    1.0F, 1.0F, 1.0F, 1.0F);

            poseStack.popPose();
        }
    }
}
