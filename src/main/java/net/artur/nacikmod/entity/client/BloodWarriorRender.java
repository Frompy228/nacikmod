package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.BloodWarriorEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

@OnlyIn(Dist.CLIENT)
public class BloodWarriorRender extends HumanoidMobRenderer<BloodWarriorEntity, BloodWarriorModel<BloodWarriorEntity>> {
    public BloodWarriorRender(EntityRendererProvider.Context context) {
        super(context, new BloodWarriorModel<>(context.bakeLayer(ModModelLayers.BLOOD_WARRIOR_LAYER)), 0.5f);

        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModModelLayers.BLOOD_WARRIOR_LAYER)),
                new HumanoidModel<>(context.bakeLayer(ModModelLayers.BLOOD_WARRIOR_LAYER)),
                context.getModelManager()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(BloodWarriorEntity entity) {
        return new ResourceLocation("minecraft", "textures/entity/player/slim/steve.png");
    }
    
}