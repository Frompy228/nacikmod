package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.entity.custom.SpartanEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class SpartanModel<T extends SpartanEntity> extends HumanoidModel<T> {
    public SpartanModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Указываем, что в правой руке есть предмет
        this.rightArmPose = ArmPose.ITEM;
        
        // Левая рука будет анимироваться в зависимости от использования щита
        if (entity.isUsingItem() && entity.getUsedItemHand() == net.minecraft.world.InteractionHand.OFF_HAND) {
            this.leftArmPose = ArmPose.BLOCK;
            // Дополнительно настраиваем угол руки для более реалистичного блокирования
            this.leftArm.xRot = -0.9424779F;
            this.leftArm.yRot = 0.5235988F;
        } else {
            this.leftArmPose = ArmPose.EMPTY;
            // Возвращаем руку в нормальное положение
            this.leftArm.xRot = 0.0F;
            this.leftArm.yRot = 0.0F;
        }
    }
} 