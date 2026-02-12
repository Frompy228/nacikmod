package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.entity.custom.KnightArcherEntity;
import net.artur.nacikmod.entity.custom.KnightCasterEntity;
import net.artur.nacikmod.entity.custom.KnightLeaderEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;

public class KnightModel<T extends LivingEntity> extends HumanoidModel<T> {

    public KnightModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        return createBodyLayer(CubeDeformation.NONE);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation deformation) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        // UV для внешнего слоя (общий с Knight Leader)
        partdefinition.addOrReplaceChild("left_arm",
            CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
            PartPose.offset(5.0F, 2.0F, 0.0F));
        partdefinition.addOrReplaceChild("left_leg",
            CubeListBuilder.create().texOffs(16, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, deformation),
            PartPose.offset(1.9F, 12.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {

        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

        // Сброс поз
        this.rightArmPose = ArmPose.EMPTY;
        this.leftArmPose = ArmPose.EMPTY;

        // Анимация лука ТОЛЬКО для KnightArcherEntity
        if (entity instanceof KnightArcherEntity archer) {
            boolean isUsingBow =
                    archer.isUsingItem()
                            && archer.getUsedItemHand() == InteractionHand.MAIN_HAND
                            && archer.getMainHandItem().is(Items.BOW);

            if (isUsingBow) {
                this.rightArmPose = ArmPose.BOW_AND_ARROW;
                this.leftArmPose = ArmPose.BOW_AND_ARROW;
            }
        }
        
        // Анимация каста для KnightCasterEntity (как у Evoker)
        if (entity instanceof KnightCasterEntity caster && caster.isCasting()) {
            // Кастомная анимация каста (как в IllagerModel)
            this.rightArm.z = 0.0F;
            this.rightArm.x = -5.0F;
            this.leftArm.z = 0.0F;
            this.leftArm.x = 5.0F;
            this.rightArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
            this.leftArm.xRot = Mth.cos(ageInTicks * 0.6662F) * 0.25F;
            this.rightArm.zRot = 2.3561945F;
            this.leftArm.zRot = -2.3561945F;
            this.rightArm.yRot = 0.0F;
            this.leftArm.yRot = 0.0F;
        }
    }

}
