package net.artur.nacikmod.entity.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class ArcherModel<T extends LivingEntity> extends HumanoidModel<T> {
    public ArcherModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return createBodyLayer(CubeDeformation.NONE);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation deformation) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();
        
        // Настраиваем UV координаты для внешнего слоя (как у утопленника)
        partdefinition.addOrReplaceChild("left_arm", 
            CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation), 
            PartPose.offset(5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg", 
            CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation), 
            PartPose.offset(1.9F, 12.0F, 0.0F));
        
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