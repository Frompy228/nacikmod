package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.entity.custom.MysteriousTraderEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class MysteriousTraderModel<T extends MysteriousTraderEntity> extends HumanoidModel<T> {

    public MysteriousTraderModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Простая логика как в ArcherModel - если есть предмет в руке, показываем его
        if (!entity.getMainHandItem().isEmpty()) {
            this.rightArmPose = ArmPose.ITEM;
        } else {
            this.rightArmPose = ArmPose.EMPTY;
        }
    }
} 