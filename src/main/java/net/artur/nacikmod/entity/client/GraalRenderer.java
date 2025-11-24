package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.GraalEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GraalRenderer extends MobRenderer<GraalEntity, GraalModel<GraalEntity>> {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(NacikMod.MOD_ID, "textures/entity/graal.png");

    public GraalRenderer(EntityRendererProvider.Context context) {
        super(context, new GraalModel<>(context.bakeLayer(ModModelLayers.GRAAL_LAYER)), 10.0f);
    }

    @Override
    public ResourceLocation getTextureLocation(GraalEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(GraalEntity entity, PoseStack poseStack, float partialTickTime) {
        poseStack.scale(10.0f, 10.0f, 10.0f);
    }

    @Override
    protected RenderType getRenderType(GraalEntity animatable, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }


}
