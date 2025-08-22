package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.entity.custom.AssassinEntity;
import net.artur.nacikmod.registry.ModItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.item.ItemStack;

public class AssassinModel<T extends AssassinEntity> extends HumanoidModel<T> {
    public AssassinModel(ModelPart root) {
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

        // Поза рук зависит от предметов в руках
        ItemStack main = entity.getMainHandItem();
        ItemStack off = entity.getOffhandItem();
        boolean mainIsDagger = !main.isEmpty() && main.getItem() == ModItems.ASSASSIN_DAGGER.get();
        boolean offIsDagger = !off.isEmpty() && off.getItem() == ModItems.ASSASSIN_DAGGER.get();

        // По умолчанию руки нейтральны
        this.rightArmPose = ArmPose.EMPTY;
        this.leftArmPose = ArmPose.EMPTY;

        if (mainIsDagger && offIsDagger) {
            // Если в обеих руках кинжалы — обе руки в позе предмета
            this.rightArmPose = ArmPose.ITEM;
            this.leftArmPose = ArmPose.ITEM;
        } else {
            // Иначе задаем позу только для тех рук, где есть предмет
            if (!main.isEmpty()) {
                this.rightArmPose = ArmPose.ITEM;
            }
            if (!off.isEmpty()) {
                this.leftArmPose = ArmPose.ITEM;
            }
        }
    }
}
