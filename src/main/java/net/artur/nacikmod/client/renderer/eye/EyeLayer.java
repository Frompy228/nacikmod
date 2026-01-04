package net.artur.nacikmod.client.renderer.eye;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.ability.EyeAbility;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.model.geom.ModelPart;

public class EyeLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {

    private static final ResourceLocation EYE_TEXTURE =
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/eye.png");

    private final EyeModel<AbstractClientPlayer> eyeModel;

    public EyeLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent,
                    EntityModelSet modelSet) {
        super(parent);
        this.eyeModel = new EyeModel<>(modelSet.bakeLayer(EyeModel.LAYER_LOCATION));
    }

    @Override
    public void render(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            float limbSwing,
            float limbSwingAmount,
            float partialTicks,
            float ageInTicks,
            float netHeadYaw,
            float headPitch
    ) {

        // 🔴 проверка способности
        if (!EyeAbility.hasEyesActive(player)) {
            return;
        }

        // синхронизация анимаций
        this.getParentModel().copyPropertiesTo(this.eyeModel);

        poseStack.pushPose();

        // привязка к голове игрока
        this.getParentModel().head.translateAndRotate(poseStack);

        poseStack.translate(0.0D, 0.0D, -0.001D);

        VertexConsumer consumer = buffer.getBuffer(
                RenderType.entityCutoutNoCull(EYE_TEXTURE)
        );

        eyeModel.renderToBuffer(
                poseStack,
                consumer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F
        );

        poseStack.popPose();
    }
}


