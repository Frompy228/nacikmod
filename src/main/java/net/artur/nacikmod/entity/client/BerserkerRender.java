package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.BerserkerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.artur.nacikmod.entity.layers.BerserkerOuterLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public class BerserkerRender extends HumanoidMobRenderer<BerserkerEntity, BerserkerModel<BerserkerEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/berserker.png");

    public BerserkerRender(EntityRendererProvider.Context context) {
        super(context, new BerserkerModel<>(context.bakeLayer(ModModelLayers.BERSERKER_LAYER)), 0.5f);

        // Добавляем слой рендера предметов в руках
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));
        
        // Добавляем слой внешней текстуры
        this.addLayer(new BerserkerOuterLayer<>(this, context.getModelSet()));
    }

    @Override
    protected void scale(BerserkerEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(2.0F, 2.0F, 2.0F); // Увеличиваем размер модели в 2 раза
    }

    @Override
    public ResourceLocation getTextureLocation(BerserkerEntity entity) {
        return TEXTURE;
    }
}