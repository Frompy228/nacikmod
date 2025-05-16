package net.artur.nacikmod.armor.models;// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.NacikMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public class LeonidHelmetModel<T extends LivingEntity> extends HumanoidModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(NacikMod.MOD_ID, "leonidhelmetmodel"), "main");
	private final ModelPart head;

	public LeonidHelmetModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
	}

	public static LayerDefinition createBodyLayer(CubeDeformation deformation) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, 0.0F);
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 45).addBox(-1.1F, -5.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(6, 56).addBox(0.1F, -5.0F, -5.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(44, 55).addBox(-0.5F, -5.0F, -5.0F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(36, 10).addBox(3.0F, -6.0F, -5.01F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 18).addBox(2.0F, -9.0F, -5.01F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 13).addBox(-5.0F, -9.0F, -5.01F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(44, 8).addBox(3.0F, -5.0F, -5.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 56).addBox(-5.0F, -5.0F, -5.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(24, 0).addBox(-1.0F, -10.0F, -4.0F, 2.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(34, 4).addBox(-2.0F, -11.0F, -4.8F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(52, 15).addBox(-1.0F, -10.0F, 4.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-1.0F, -13.0F, -4.0F, 2.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(52, 43).addBox(-1.0F, -10.0F, 6.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(14, 8).addBox(-5.0F, -8.0F, -4.0F, 1.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(18, 13).addBox(4.0F, -8.0F, -4.0F, 1.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(6, 33).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(1, 1).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.8692F, -5.6064F, -4.51F, 0.0F, 0.0F, -0.2269F));

		PartDefinition cube_r2 = head.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(12, 5).addBox(0.0F, -8.0F, -5.0F, 1.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition cube_r3 = head.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(13, 4).addBox(0.0F, -9.0F, -4.0F, 1.0F, 10.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -8.0F, 0.0F, 0.0F, 0.0F, -1.5708F));

		PartDefinition cube_r4 = head.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(52, 0).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -7.6F, 5.8F, -1.6134F, 0.0F, 0.0001F));

		PartDefinition cube_r5 = head.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(48, 48).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.2F, 5.8F, -1.4912F, 0.0F, 0.0F));

		PartDefinition cube_r6 = head.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(40, 48).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, 5.6F, -1.4476F, 0.0085F, 0.0001F));

		PartDefinition cube_r7 = head.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(44, 0).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 5.4F, -1.2294F, 0.0085F, 0.0001F));

		PartDefinition cube_r8 = head.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(36, 26).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.3F, 4.9F, -1.0985F, 0.0085F, 0.0001F));

		PartDefinition cube_r9 = head.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(36, 15).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.3F, 4.3F, -0.924F, 0.0085F, 0.0001F));

		PartDefinition cube_r10 = head.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(36, 37).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.1F, 3.6F, -0.7494F, 0.0085F, 0.0001F));

		PartDefinition cube_r11 = head.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(44, 15).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.5F, 3.1F, -0.444F, 0.0085F, 0.0001F));

		PartDefinition cube_r12 = head.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(32, 48).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.9F, 2.0F, -0.252F, 0.0085F, 0.0001F));

		PartDefinition cube_r13 = head.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(24, 45).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -16.0F, 1.0F, -0.0251F, 0.0085F, 0.0001F));

		PartDefinition cube_r14 = head.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(44, 37).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.9F, -0.2F, 0.1494F, 0.0085F, 0.0001F));

		PartDefinition cube_r15 = head.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(44, 26).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.7F, -0.7F, 0.4548F, 0.0085F, 0.0001F));

		PartDefinition cube_r16 = head.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(16, 45).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.2F, -1.5F, 0.673F, 0.0085F, 0.0001F));

		PartDefinition cube_r17 = head.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(8, 45).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.5F, -3.0F, 1.0472F, 0.0F, 0.0F));

		PartDefinition cube_r18 = head.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(0, 45).addBox(-1.0F, -4.5F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -14.4F, -2.4F, 0.8465F, 0.0F, 0.0F));

		PartDefinition cube_r19 = head.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(53, 28).addBox(0.0F, -6.0F, -1.01F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5912F, -8.0067F, -4.0F, 0.0F, 0.0F, -0.9163F));

		PartDefinition cube_r20 = head.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(52, 28).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3912F, -8.2067F, -4.0F, 0.0F, 0.0F, -0.9163F));

		PartDefinition cube_r21 = head.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(52, 21).addBox(-1.0F, -6.0F, -1.0F, 1.6088F, 0.7933F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(52, 21).addBox(-1.0F, -5.2067F, -1.0F, 2.0F, 5.2067F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3912F, -8.2067F, -4.0F, 0.0F, 0.0F, 0.9163F));

		PartDefinition cube_r22 = head.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(40, 55).addBox(-1.0F, -2.0F, -1.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.5F, -2.3F, -4.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition cube_r23 = head.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(48, 54).addBox(-1.0F, -2.0F, -1.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -2.4F, -4.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition cube_r24 = head.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(52, 35).addBox(0.0F, -2.0F, -1.0F, 1.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -2.4F, -4.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition cube_r25 = head.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(52, 54).addBox(0.0F, -2.0F, -1.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -2.3F, -4.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition cube_r26 = head.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(22, 56).addBox(0.0F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, -1.2F, -4.0F, 0.0F, 0.0F, 0.1222F));

		PartDefinition cube_r27 = head.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(14, 56).addBox(-1.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4F, -1.1F, -4.0F, 0.0F, 0.0F, -0.5323F));

		PartDefinition cube_r28 = head.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(10, 56).addBox(0.0F, -2.0F, -1.0F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4F, -1.1F, -4.0F, 0.0F, 0.0F, 0.5323F));

		PartDefinition cube_r29 = head.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(30, 10).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6F, -2.8F, -5.0F, 0.0F, 0.0F, 0.5323F));

		PartDefinition cube_r30 = head.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(24, 10).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6F, -2.8F, -5.0F, 0.0F, 0.0F, -0.5323F));

		PartDefinition cube_r31 = head.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(18, 56).addBox(-1.0F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, -1.2F, -4.0F, 0.0F, 0.0F, -0.1222F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}