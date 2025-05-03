package net.artur.nacikmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.registry.ModEffects;

public class LastMagicAuraRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation AURA_TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/last_magic_aura.png");
    private static final float AURA_ALPHA = 1f;

    public LastMagicAuraRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                      float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (!player.hasEffect(ModEffects.MANA_LAST_MAGIC.get())) {
            return;
        }

        // Копируем позу основной модели для синхронизации с конечностями
        this.getParentModel().copyPropertiesTo(this.getParentModel());
        this.getParentModel().setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Get the vertex consumer for the aura texture
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(AURA_TEXTURE));

        // Рендерим ауру
        this.getParentModel().renderToBuffer(
            poseStack,
            vertexConsumer,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            1.0f, 1.0f, 1.0f, AURA_ALPHA
        );
    }
} 