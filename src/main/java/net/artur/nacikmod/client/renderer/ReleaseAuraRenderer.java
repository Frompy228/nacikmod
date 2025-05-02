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
import net.artur.nacikmod.item.ability.ManaRelease;

public class ReleaseAuraRenderer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation AURA_TEXTURE_LEVEL_1 = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/release_aura.png");
    private static final ResourceLocation AURA_TEXTURE_LEVEL_2 = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/release_aura2.png");
    private static final float AURA_ALPHA = 0.2f;

    public ReleaseAuraRenderer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player,
                      float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // Проверяем только состояние способности, не зависимо от предмета в руке
        if (!ManaRelease.isReleaseActive(player)) {
            return;
        }

        // Копируем позу основной модели для синхронизации с конечностями
        this.getParentModel().copyPropertiesTo(this.getParentModel());
        this.getParentModel().setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Определяем текущий уровень способности
        ManaRelease.Level currentLevel = ManaRelease.getCurrentLevel(player);
        
        // Выбираем текстуру в зависимости от уровня
        ResourceLocation auraTexture = currentLevel.name.equals("Level 1") ? AURA_TEXTURE_LEVEL_1 : AURA_TEXTURE_LEVEL_2;

        // Get the vertex consumer for the aura texture
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(auraTexture));

        // Рендерим ауру точно по размеру модели
        this.getParentModel().renderToBuffer(
            poseStack,
            vertexConsumer,
            packedLight,
            OverlayTexture.NO_OVERLAY,
            1.0f, 1.0f, 1.0f, AURA_ALPHA
        );
    }
} 