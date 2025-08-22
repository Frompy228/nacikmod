package net.artur.nacikmod.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.AssassinEntity;
import net.artur.nacikmod.entity.client.AssassinModel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class AssassinOuterLayer<T extends AssassinEntity> extends RenderLayer<T, AssassinModel<T>> {
    private final AssassinModel<T> model;

    public AssassinOuterLayer(RenderLayerParent<T, AssassinModel<T>> parent, EntityModelSet modelSet) {
        super(parent);
        this.model = new AssassinModel<>(modelSet.bakeLayer(net.artur.nacikmod.entity.client.ModModelLayers.ASSASSIN_OUTER_LAYER));
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, T entity, 
                      float limbSwing, float limbSwingAmount, float partialTicks, 
                      float ageInTicks, float netHeadYaw, float headPitch) {
        
        // Определяем внешнюю текстуру в зависимости от основной
        ResourceLocation outerTexture = getOuterTexture(entity);
        
        coloredCutoutModelCopyLayerRender(this.getParentModel(), this.model, outerTexture, 
                                        poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, 
                                        ageInTicks, netHeadYaw, headPitch, partialTicks, 1.0F, 1.0F, 1.0F);
    }
    
    private ResourceLocation getOuterTexture(AssassinEntity entity) {
        // Выбираем случайную внешнюю текстуру из 8 вариантов
        int outerTextureIndex = Math.abs(entity.getId()) % 8; // 8 внешних текстур
        
        if (outerTextureIndex == 0) {
            return new ResourceLocation(NacikMod.MOD_ID, "textures/entity/assassin_outer_layer.png");
        } else {
            return new ResourceLocation(NacikMod.MOD_ID, "textures/entity/assassin_" + outerTextureIndex + "_outer_layer.png");
        }
    }
}
