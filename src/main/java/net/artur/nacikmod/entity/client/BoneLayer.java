package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.item.BoneItem;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Этот слой рендерит BoneModel поверх игрока,
 * если у него есть активный BoneItem в инвентаре.
 */
public class BoneLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/bone.png");

    private final BoneModel<AbstractClientPlayer> model;

    public BoneLayer(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
        super(parent);
        this.model = new BoneModel<>(BoneModel.createBodyLayer().bakeRoot());
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                       AbstractClientPlayer player, float limbSwing, float limbSwingAmount,
                       float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        // Проверяем, есть ли у игрока BoneItem в инвентаре
        boolean hasBoneItem = false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof BoneItem) {
                hasBoneItem = true;
                break;
            }
        }

        if (!hasBoneItem) return; // нет предмета — не рендерим

        // Синхронизируем анимации модели с телом игрока
        this.model.setupAnim(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        poseStack.pushPose();

        // Привязка к телу игрока - используем позицию и вращение тела
        this.getParentModel().body.translateAndRotate(poseStack);

        // Позиционируем модель относительно тела
        // BoneModel имеет offset Y=24, поэтому сдвигаем вниз для правильного позиционирования
        poseStack.translate(0.0D, 0.0D, 0.0D);
        poseStack.scale(1.0F, 1.0F, 1.0F);

        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        model.renderToBuffer(
                poseStack,
                vertexconsumer,
                packedLight,
                LivingEntityRenderer.getOverlayCoords(player, 0.0F),
                1.0F, 1.0F, 1.0F, 1.0F
        );
        poseStack.popPose();
    }
}

