package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.KnightBossEntity;
import net.artur.nacikmod.entity.layers.KnightOuterLayer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KnightBossRender extends HumanoidMobRenderer<KnightBossEntity, KnightModel<KnightBossEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/knights/knight_boss.png");
    private static final ResourceLocation OUTER_LAYER = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/knights/knight_boss_out_layer.png");

    public KnightBossRender(EntityRendererProvider.Context context) {
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
    public ResourceLocation getTextureLocation(KnightBossEntity entity) {
        return TEXTURE;
    }
}
