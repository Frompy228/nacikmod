package net.artur.nacikmod.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class  SuppressingGateModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "custommodel"), "main");
	private final ModelPart bb_main;

	public  SuppressingGateModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-24.0F, -57.0F, -5.0F, 10.0F, 57.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(40, 0).addBox(14.0F, -57.0F, -5.0F, 10.0F, 57.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(80, 0).addBox(37.0F, -57.2F, -3.0F, 0.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(80, 20).addBox(37.0F, -57.2F, 2.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(80, 10).addBox(-37.0F, -57.2F, 2.0F, 0.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(80, 26).addBox(-37.0F, -57.2F, -3.0F, 0.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(34, 67).addBox(-4.5F, -19.0F, -6.0F, 9.0F, 38.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.8F, -60.7F, 0.0F, 0.0F, 0.0F, 1.6232F));

		PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(76, 67).addBox(-4.5F, -18.0F, -6.0F, 9.0F, 36.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.8F, -60.6F, 0.0F, 0.0F, 0.0F, 1.5184F));

		PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 67).addBox(-4.0F, -30.0F, -4.0F, 9.0F, 60.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -46.0F, 0.0F, 0.0F, 0.0F, 1.5708F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}