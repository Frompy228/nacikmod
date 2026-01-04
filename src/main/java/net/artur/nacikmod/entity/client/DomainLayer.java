package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.ability.DomainAbility;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;

/**
 * Этот слой рендерит кастомную модель DomainModel поверх игрока,
 * если у него активен Domain через Ability систему.
 * Видно ВСЕМ игрокам благодаря синхронизации через AbilityStateManager.
 */
public class DomainLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/domain_aura.png");

    private final DomainModel<AbstractClientPlayer> model;

    public DomainLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        this.model = new DomainModel<>(DomainModel.createBodyLayer().bakeRoot());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        // Проверяем: активен ли Domain через Ability систему
        if (!DomainAbility.isDomainActive(player)) return; // не активен — не рендерим

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
                1.0F, 1.0F, 1.0F, 0.1F
        );
        poseStack.popPose();
    }
}
