package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.NacikMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class MovementSealModel<T extends LivingEntity> extends EntityModel<T>  {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(NacikMod.MOD_ID, "movementseal"), "main");
    private final ModelPart bb_main;

    public MovementSealModel(ModelPart root) {
        this.bb_main = root.getChild("bb_main");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(38, 6).addBox(-3.0F, -1.0F, 0.8292F, 5.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -20.516F, 9.2292F, 0.5444F, 0.2635F, 0.1564F));

        PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(38, 23).addBox(0.0F, -2.0F, 0.7292F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, -24.316F, 9.5292F, 0.5444F, -0.2635F, -0.1564F));

        PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(38, 2).addBox(-2.5F, -0.5F, 5.6322F, 5.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8763F, -24.262F, 5.5322F, 0.569F, 0.1886F, 0.1104F));

        PartDefinition cube_r4 = bb_main.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(38, 4).addBox(-3.0F, -1.0F, 0.8292F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(38, 19).addBox(-1.0F, -1.0F, 1.8292F, 1.0F, 1.0F, 2.6708F, new CubeDeformation(0.0F))
                .texOffs(0, 38).addBox(-1.0F, -1.0F, -16.5F, 1.0F, 1.0F, 17.3292F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -21.116F, 9.3292F, 0.5444F, 0.2635F, 0.1564F));

        PartDefinition cube_r5 = bb_main.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(38, 30).addBox(-1.0F, 0.0F, 2.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(38, 11).addBox(-1.0F, -3.0F, 1.7292F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-1.0F, 0.0F, -16.5F, 1.0F, 1.0F, 18.2292F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -24.116F, 9.2292F, 0.5444F, -0.2635F, -0.1564F));

        PartDefinition cube_r6 = bb_main.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(38, 7).addBox(-0.5F, -0.5F, 6.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(38, 0).addBox(-3.5F, -0.5F, 5.6322F, 7.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 19).addBox(-0.5F, -0.5F, -12.0F, 1.0F, 1.0F, 17.6322F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.8763F, -24.962F, 5.6322F, 0.569F, 0.1886F, 0.1104F));

        PartDefinition cube_r7 = bb_main.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(38, 37).addBox(-1.0F, -2.0F, 1.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.1276F, -21.4122F, 12.0931F, 0.0F, 0.2443F, 0.0F));

        PartDefinition cube_r8 = bb_main.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(36, 38).addBox(-1.0F, -2.0F, 1.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.1451F, -23.3959F, 12.4668F, 0.0F, -0.2094F, 0.0F));

        PartDefinition cube_r9 = bb_main.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(38, 33).addBox(-1.0F, -2.0F, 1.0F, 1.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0057F, -27.1463F, 12.2989F, 0.0F, 0.3142F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
        // Оставь пустым, если анимация не требуется
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}