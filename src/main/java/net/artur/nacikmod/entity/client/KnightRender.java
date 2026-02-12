package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.NacikMod;
import net.artur.nacikmod.entity.custom.KnightEntity;
import net.artur.nacikmod.entity.layers.KnightOuterLayer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class KnightRender extends HumanoidMobRenderer<KnightEntity, KnightModel<KnightEntity>> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(NacikMod.MOD_ID, "textures/entity/knights/knight.png");

    public KnightRender(EntityRendererProvider.Context context) {
        super(context, new KnightModel<>(context.bakeLayer(ModModelLayers.KNIGHT_LAYER)), 0.5f);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()
        ));
        this.addLayer(new KnightOuterLayer<>(this, context.getModelSet()));
    }

    @Override
    protected void setupRotations(KnightEntity entity, com.mojang.blaze3d.vertex.PoseStack matrixStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.setupRotations(entity, matrixStack, ageInTicks, rotationYaw, partialTicks);
    }

    @Override
    public void render(KnightEntity entity, float entityYaw, float partialTicks, com.mojang.blaze3d.vertex.PoseStack matrixStack, MultiBufferSource buffer, int packedLight) {

        HumanoidArm mainArm = entity.getMainArm();
        HumanoidArm offArm = mainArm == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;

        // --- ПИТЬЕ / ЗЕЛЬЯ ---
        if (entity.isDrinking()) {
            if (mainArm == HumanoidArm.RIGHT) {
                this.model.rightArmPose = HumanoidModel.ArmPose.TOOT_HORN;
                this.model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
            } else {
                this.model.leftArmPose = HumanoidModel.ArmPose.TOOT_HORN;
                this.model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            }
        }
        // --- ОСНОВНОЙ ПРЕДМЕТ / МЕЧ ---
        else if (!entity.getMainHandItem().isEmpty()) {
            if (mainArm == HumanoidArm.RIGHT) {
                this.model.rightArmPose = HumanoidModel.ArmPose.ITEM;
                this.model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
            } else {
                this.model.leftArmPose = HumanoidModel.ArmPose.ITEM;
                this.model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            }
        }
        // --- ПУСТЫЕ РУКИ ---
        else {
            this.model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
            this.model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        }

        // --- ЩИТ / ОФФ-ХЭНД ---
        if (entity.isUsingItem() && entity.getUsedItemHand() == InteractionHand.OFF_HAND) {
            if (mainArm == HumanoidArm.RIGHT) {
                this.model.leftArmPose = HumanoidModel.ArmPose.BLOCK;
            } else {
                this.model.rightArmPose = HumanoidModel.ArmPose.BLOCK;
            }
        }

        super.render(entity, entityYaw, partialTicks, matrixStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(KnightEntity entity) {
        return TEXTURE;
    }
}