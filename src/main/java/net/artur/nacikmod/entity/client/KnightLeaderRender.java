package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.KnightLeaderEntity;
import net.artur.nacikmod.entity.layers.KnightOuterLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KnightLeaderRender extends HumanoidMobRenderer<KnightLeaderEntity, KnightModel<KnightLeaderEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/knights/knight_leader.png");

    public KnightLeaderRender(EntityRendererProvider.Context context) {
        super(context, new KnightModel<>(context.bakeLayer(ModModelLayers.KNIGHT_LAYER)), 0.5f);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
        this.addLayer(new KnightOuterLayer<>(this, context.getModelSet()));
    }

    @Override
    public void render(KnightLeaderEntity entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {

        HumanoidArm mainArm = entity.getMainArm(); // Определяем основную руку моба
        HumanoidArm offArm = mainArm == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;

        // --- ЛОГИКА ПИТЬЯ ЗЕЛИЙ ---
        if (entity.isDrinking()) {
            if (mainArm == HumanoidArm.RIGHT) {
                this.model.rightArmPose = HumanoidModel.ArmPose.TOOT_HORN;
                this.model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
            } else {
                this.model.leftArmPose = HumanoidModel.ArmPose.TOOT_HORN;
                this.model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            }
        }
        // --- ЛОГИКА ОСНОВНОГО ПРЕДМЕТА (меч, оружие) ---
        else if (!entity.getMainHandItem().isEmpty()) {
            if (mainArm == HumanoidArm.RIGHT) {
                this.model.rightArmPose = HumanoidModel.ArmPose.ITEM;
                this.model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
            } else {
                this.model.leftArmPose = HumanoidModel.ArmPose.ITEM;
                this.model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            }
        }
        // --- Ничего не держит ---
        else {
            this.model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            this.model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        }

        // --- ЛОГИКА ЩИТА / ОФФ-ХЭНДА ---
        if (entity.isUsingItem() && entity.getUsedItemHand() == InteractionHand.OFF_HAND) {
            if (mainArm == HumanoidArm.RIGHT) {
                this.model.leftArmPose = HumanoidModel.ArmPose.BLOCK;
            } else {
                this.model.rightArmPose = HumanoidModel.ArmPose.BLOCK;
            }
        }

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(KnightLeaderEntity entity) {
        return TEXTURE;
    }
}