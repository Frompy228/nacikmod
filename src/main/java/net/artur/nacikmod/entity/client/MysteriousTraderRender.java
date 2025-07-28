package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.MysteriousTraderEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import com.mojang.blaze3d.vertex.PoseStack;

@OnlyIn(Dist.CLIENT)
public class MysteriousTraderRender extends HumanoidMobRenderer<MysteriousTraderEntity, MysteriousTraderModel<MysteriousTraderEntity>> {
    public MysteriousTraderRender(EntityRendererProvider.Context context) {
        super(context, new MysteriousTraderModel<>(context.bakeLayer(ModModelLayers.MYSTERIOUS_TRADER_LAYER)), 0.5f);

        // Добавляем слой рендера предметов в руках (точно как в ArcherRender)
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));
    }

    @Override
    public ResourceLocation getTextureLocation(MysteriousTraderEntity entity) {
        return new ResourceLocation(NacikMod.MOD_ID, "textures/entity/mysterious_trader.png");
    }
    
    @Override
    public void render(MysteriousTraderEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
} 