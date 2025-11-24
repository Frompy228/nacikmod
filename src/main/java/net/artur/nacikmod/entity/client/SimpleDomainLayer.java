package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModEffects;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * Этот слой рендерит кастомную модель SimplePossessionModel поверх игрока,
 * если у него активен эффект EffectSimplePossession.
 * Видно ВСЕМ игрокам, если эффект применяется сервером.
 */
public class SimpleDomainLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/simple_domain_aura.png");

    private final SimpleDomainModel<AbstractClientPlayer> model;

    public SimpleDomainLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        this.model = new SimpleDomainModel<>(SimpleDomainModel.createBodyLayer().bakeRoot());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        // Проверяем: есть ли эффект
        MobEffectInstance effect = player.getEffect(ModEffects.EFFECT_SIMPLE_DOMAIN.get());
        if (effect == null) return; // нет эффекта — не рендерим

        // Если есть эффект, отрисовываем модель
        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.0D); // позиция модели
        poseStack.scale(1.0F, 1.0F, 1.0F); // масштаб

        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        model.renderToBuffer(
                poseStack,
                vertexconsumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F),
                1.0F, 1.0F, 1.0F, 0.25F
        );
        poseStack.popPose();
    }
}
