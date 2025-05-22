package net.artur.nacikmod.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.artur.nacikmod.armor.models.DarkSphereModel;
import net.artur.nacikmod.entity.client.ModModelLayers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class DarkSphereRenderer implements ICurioRenderer {
    private static boolean init;
    private static DarkSphereModel SPHERE_MODEL;

    public static void initializeModels() {
        init = true;
        SPHERE_MODEL = new DarkSphereModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModModelLayers.DARK_SPHERE_LAYER));
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(ItemStack stack,
                                                                        SlotContext slotContext,
                                                                        PoseStack poseStack,
                                                                        RenderLayerParent<T, M> renderLayerParent,
                                                                        MultiBufferSource buffer,
                                                                        int light,
                                                                        float limbSwing,
                                                                        float limbSwingAmount,
                                                                        float partialTicks,
                                                                        float ageInTicks,
                                                                        float netHeadYaw,
                                                                        float headPitch) {
        if (!init) {
            initializeModels();
        }

        LivingEntity entity = slotContext.entity();
        SPHERE_MODEL.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        SPHERE_MODEL.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTicks);

        poseStack.pushPose();
        // Adjust position to match the head
        poseStack.translate(0.0D, 0.0D, 0.0D);
        poseStack.scale(1.0F, 1.0F, 1.0F);

        SPHERE_MODEL.renderToBuffer(poseStack, 
            buffer.getBuffer(RenderType.entityCutoutNoCull(SPHERE_MODEL.getTextureLocation(entity))),
            light, 
            OverlayTexture.NO_OVERLAY, 
            1.0F, 1.0F, 1.0F, 1.0F);

        poseStack.popPose();
    }
} 