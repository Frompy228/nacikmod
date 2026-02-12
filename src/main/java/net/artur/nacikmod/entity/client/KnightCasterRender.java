package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.KnightCasterEntity;
import net.artur.nacikmod.entity.layers.KnightOuterLayer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KnightCasterRender extends HumanoidMobRenderer<KnightCasterEntity, KnightModel<KnightCasterEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/knights/knight_caster.png");
    private static final ResourceLocation OUTER_LAYER = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/knights/knight_caster_out_layer.png");

    public KnightCasterRender(EntityRendererProvider.Context context) {
        super(context, new KnightModel<>(context.bakeLayer(ModModelLayers.KNIGHT_LAYER)), 0.5f);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new net.minecraft.client.model.HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new net.minecraft.client.model.HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));
        this.addLayer(new KnightOuterLayer<>(this, context.getModelSet(), OUTER_LAYER));
    }

    @Override
    public ResourceLocation getTextureLocation(KnightCasterEntity entity) {
        return TEXTURE;
    }
    
    @Override
    public void render(KnightCasterEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        // Рендерим иллюзии, если кастер невидим (как у Illusioner)
        if (entity.isInvisible()) {
            Vec3[] offsets = entity.getIllusionOffsets(partialTicks);
            float f = this.getBob(entity, partialTicks);
            
            for (int i = 0; i < offsets.length; ++i) {
                poseStack.pushPose();
                poseStack.translate(
                    offsets[i].x + (double)Mth.cos((float)i + f * 0.5F) * 0.025D,
                    offsets[i].y + (double)Mth.cos((float)i + f * 0.75F) * 0.0125D,
                    offsets[i].z + (double)Mth.cos((float)i + f * 0.7F) * 0.025D
                );
                super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
                poseStack.popPose();
            }
        } else {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
    }
    
    protected boolean isBodyVisible(KnightCasterEntity entity) {
        return true;
    }
}
