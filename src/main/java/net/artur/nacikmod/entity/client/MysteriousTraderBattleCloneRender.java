package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.MysteriousTraderBattleCloneEntity;
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
public class MysteriousTraderBattleCloneRender extends HumanoidMobRenderer<MysteriousTraderBattleCloneEntity, MysteriousTraderBattleCloneModel<MysteriousTraderBattleCloneEntity>> {
    public MysteriousTraderBattleCloneRender(EntityRendererProvider.Context context) {
        super(context, new MysteriousTraderBattleCloneModel<>(context.bakeLayer(ModModelLayers.MYSTERIOUS_TRADER_BATTLE_CLONE_LAYER)), 0.5f);

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
    public ResourceLocation getTextureLocation(MysteriousTraderBattleCloneEntity entity) {
        return new ResourceLocation(NacikMod.MOD_ID, "textures/entity/mysterious_trader.png");
    }
} 