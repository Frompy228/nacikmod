package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.SpartanEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SpartanRender extends HumanoidMobRenderer<SpartanEntity, SpartanModel<SpartanEntity>> {
    public SpartanRender(EntityRendererProvider.Context context) {
        super(context, new SpartanModel<>(context.bakeLayer(ModModelLayers.SPARTAN_LAYER)), 0.5f);

        // Добавляем слой рендера предметов в руках
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(SpartanEntity entity) {
        return new ResourceLocation(NacikMod.MOD_ID, "textures/entity/spartan.png");
    }
} 