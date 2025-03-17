package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.LanserEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;

public class LanserRender extends HumanoidMobRenderer<LanserEntity, LanserModel<LanserEntity>> {
    public LanserRender(EntityRendererProvider.Context context) {
        super(context, new LanserModel<>(context.bakeLayer(ModModelLayers.LANSER_LAYER)), 0.5f);

        // Добавляем слой рендера предметов в руках
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(LanserEntity entity) {
        return new ResourceLocation(NacikMod.MOD_ID, "textures/entity/lanser.png");
    }
}
