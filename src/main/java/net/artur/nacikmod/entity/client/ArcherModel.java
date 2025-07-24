package net.artur.nacikmod.entity.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class ArcherModel<T extends LivingEntity> extends HumanoidModel<T> {
    public ArcherModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        boolean isBow = entity.isUsingItem()
            && entity.getUsedItemHand() == net.minecraft.world.InteractionHand.MAIN_HAND
            && entity.getMainHandItem().getItem().getClass().getSimpleName().toLowerCase().contains("bow");
        if (isBow) {
            this.rightArmPose = ArmPose.BOW_AND_ARROW;
            this.leftArmPose = ArmPose.BOW_AND_ARROW;
        } else {
            this.rightArmPose = entity.getMainHandItem().getItem().getClass().getSimpleName().toLowerCase().contains("sword") ? ArmPose.ITEM : ArmPose.EMPTY;
            this.leftArmPose = entity.getOffhandItem().getItem().getClass().getSimpleName().toLowerCase().contains("sword") ? ArmPose.ITEM : ArmPose.EMPTY;
        }
    }
} 