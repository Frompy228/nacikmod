package net.artur.nacikmod.entity.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.artur.nacikmod.entity.custom.MysteriousTraderBattleCloneEntity;

public class MysteriousTraderBattleCloneModel<T extends MysteriousTraderBattleCloneEntity> extends HumanoidModel<T> {

    public MysteriousTraderBattleCloneModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Указываем, что в правой руке есть предмет (mana sword)
        this.rightArmPose = ArmPose.ITEM;
    }
} 