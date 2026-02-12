package net.artur.nacikmod.entity.client;// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.artur.nacikmod.NacikMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class BoneModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(NacikMod.MOD_ID, "bone"), "main");
	private final ModelPart body;

	public BoneModel(ModelPart root) {
		this.body = root.getChild("body");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(7, 16).addBox(-0.8F, 5.4537F, 5.9613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(20, 16).addBox(-1.0F, 5.4537F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(9, 16).addBox(-0.8F, 5.4537F, 7.7613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(6, 15).addBox(0.4F, 5.4537F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(6, 0).addBox(-0.8F, -9.8463F, 6.3613F, 0.2F, 18.7F, 1.2F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-0.6F, -9.8463F, 6.1613F, 1.2F, 18.7F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(6, 0).addBox(0.6F, -9.8463F, 6.3613F, 0.2F, 18.7F, 1.2F, new CubeDeformation(0.0F))
				.texOffs(13, 0).addBox(-0.8F, -9.5463F, 7.7613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(16, 28).addBox(-0.8F, -6.5463F, 5.9613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(4, 17).addBox(-0.8F, -6.5463F, 7.7613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(3, 5).addBox(-0.8F, -3.5463F, 5.9613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(7, 12).addBox(-0.8F, -3.5463F, 7.7613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(17, 9).addBox(-0.8F, -0.5463F, 5.9613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(9, 16).addBox(-0.8F, -0.5463F, 7.7613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(20, 18).addBox(-0.8F, 2.4537F, 5.9613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(24, 14).addBox(-0.8F, 2.4537F, 7.7613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(26, 12).addBox(-0.8F, -9.5463F, 5.9613F, 1.6F, 2.8F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(11, 11).addBox(0.4F, -9.5463F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(10, 16).addBox(-1.0F, -9.5463F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(4, 17).addBox(-1.0F, -6.5463F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(6, 16).addBox(0.4F, -6.5463F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(9, 21).addBox(0.4F, -3.5463F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(9, 18).addBox(-1.0F, -3.5463F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(17, 18).addBox(0.4F, -0.5463F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(27, 0).addBox(-1.0F, -0.5463F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(0.4F, 2.4537F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(16, 27).addBox(-1.0F, 2.4537F, 6.1613F, 0.6F, 2.8F, 1.6F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.8463F, 0.3387F));

		PartDefinition cube_r1 = body.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(25, 18).addBox(2.1525F, -0.65F, 4.5911F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6193F, 10.0037F, -0.2472F, -3.0457F, 0.4259F, -3.1019F));

		PartDefinition cube_r2 = body.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(12, 4).addBox(2.0064F, -0.65F, 3.9714F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6193F, 10.0037F, -0.2472F, -3.0325F, 0.6429F, -3.076F));

		PartDefinition cube_r3 = body.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(20, 20).addBox(1.3936F, -0.65F, 2.931F, 2.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6193F, 10.0037F, -0.2472F, -2.9823F, 0.989F, -3.0082F));

		PartDefinition cube_r4 = body.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(20, 6).addBox(1.1224F, -0.65F, 1.587F, 2.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6193F, 10.0037F, -0.2472F, -2.1099F, 1.4691F, -2.1122F));

		PartDefinition cube_r5 = body.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(15, 8).addBox(-0.8F, -2.5F, 0.8F, 1.6F, 3.9F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(24, 9).addBox(-0.8F, -2.5F, -1.0F, 1.6F, 3.9F, 0.2F, new CubeDeformation(0.0F))
				.texOffs(8, 19).addBox(-0.6F, -2.5F, -0.8F, 1.2F, 3.9F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(23, 26).addBox(-1.0F, -2.5F, -0.8F, 0.6F, 3.9F, 1.6F, new CubeDeformation(0.0F))
				.texOffs(13, 14).addBox(0.4F, -2.5F, -0.8F, 0.6F, 3.9F, 1.6F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 10.8537F, 6.7613F, -0.0873F, 0.0F, 0.0F));

		PartDefinition cube_r6 = body.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 4).addBox(-2.9704F, -0.65F, 6.5337F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 3.1055F, 0.2529F, 3.1326F));

		PartDefinition cube_r7 = body.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(3, 15).addBox(-2.9608F, -0.65F, 6.9214F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 3.1032F, 0.4273F, 3.1257F));

		PartDefinition cube_r8 = body.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(3, 7).addBox(-2.4813F, -0.65F, 7.3531F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 3.0979F, 0.6453F, 3.1153F));

		PartDefinition cube_r9 = body.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(5, 3).addBox(-1.6669F, -0.65F, 7.6438F, 2.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 3.0776F, 0.9939F, 3.0879F));

		PartDefinition cube_r10 = body.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(18, 12).addBox(0.8283F, -0.65F, 7.1986F, 2.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 2.5532F, 1.5079F, 2.5541F));

		PartDefinition cube_r11 = body.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(8, 12).addBox(-1.4819F, -0.65F, 7.0918F, 3.1F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 0.7857F, 1.5214F, 0.7851F));

		PartDefinition cube_r12 = body.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(8, 12).addBox(-3.0006F, -0.75F, 7.1994F, 3.5F, 1.5F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 0.1386F, 1.3154F, 0.1341F));

		PartDefinition cube_r13 = body.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(8, 8).addBox(-1.1416F, -0.85F, 7.2865F, 3.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 0.0477F, 0.7499F, 0.0325F));

		PartDefinition cube_r14 = body.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(8, 4).addBox(0.5948F, -0.95F, 5.7477F, 4.5F, 1.9F, 2.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 0.035F, 0.0523F, 0.0018F));

		PartDefinition cube_r15 = body.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(14, 19).addBox(0.0F, -2.0F, 0.0F, 1.3F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(2, 4).addBox(0.0F, 6.0F, 0.0F, 1.3F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(9, 18).addBox(0.0F, 2.0F, 0.0F, 1.3F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.778F, -1.3463F, -5.7159F, 0.0F, 3.098F, 0.0F));

		PartDefinition cube_r16 = body.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(8, 15).addBox(0.0F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(1, 18).addBox(0.0F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(2, 9).addBox(0.0F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.2006F, -1.3463F, -5.524F, 0.0F, 3.0631F, 0.0F));

		PartDefinition cube_r17 = body.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(8, 15).addBox(0.0F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(21, 10).addBox(0.0F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(19, 6).addBox(0.0F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.4348F, -1.3463F, -3.7585F, 0.0F, 2.4522F, 0.0F));

		PartDefinition cube_r18 = body.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(8, 15).addBox(0.0F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(22, 9).addBox(0.0F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 4).addBox(0.0F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.1147F, -1.3463F, -2.1554F, 0.0F, 2.0595F, 0.0F));

		PartDefinition cube_r19 = body.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(8, 6).addBox(0.0F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(6, 3).addBox(0.0F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(17, 9).addBox(0.0F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.3697F, -1.3463F, -0.2717F, 0.0F, 1.7541F, 0.0F));

		PartDefinition cube_r20 = body.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(15, 10).addBox(0.0F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(17, 4).addBox(0.0F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 23).addBox(0.0F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(10.0464F, -1.3463F, 1.6014F, 0.0F, 1.4486F, 0.0F));

		PartDefinition cube_r21 = body.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(8, 15).addBox(0.0F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 26).addBox(0.0F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(12, 10).addBox(0.0F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.388F, -1.3463F, 3.6512F, 0.0F, 1.2741F, 0.0F));

		PartDefinition cube_r22 = body.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(14, 19).addBox(0.0F, -2.0F, 0.0F, 1.7F, 1.7F, 2.1F, new CubeDeformation(0.0F))
				.texOffs(8, 20).addBox(0.0F, 6.0F, 0.0F, 1.7F, 1.7F, 2.1F, new CubeDeformation(0.0F))
				.texOffs(20, 11).addBox(0.0F, 2.0F, 0.0F, 1.7F, 1.7F, 2.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.6843F, -1.3463F, 4.0238F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r23 = body.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(8, 8).addBox(0.0F, -2.1F, -0.1F, 3.5F, 1.9F, 2.3F, new CubeDeformation(0.0F))
				.texOffs(8, 8).addBox(0.0F, 5.9F, -0.1F, 3.5F, 1.9F, 2.3F, new CubeDeformation(0.0F))
				.texOffs(4, 5).addBox(0.0F, 1.9F, -0.1F, 3.5F, 1.9F, 2.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.1318F, -1.3463F, 6.1557F, 0.0F, 0.6981F, 0.0F));

		PartDefinition cube_r24 = body.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(8, 0).addBox(0.0F, -2.2F, -0.2F, 6.5F, 2.1F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(0, 14).addBox(0.0F, 5.8F, -0.2F, 6.5F, 2.1F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(1, 13).addBox(0.0F, 1.8F, -0.2F, 6.5F, 2.1F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.9F, -1.3463F, 5.9613F, 0.0F, 0.0524F, 0.0F));

		PartDefinition cube_r25 = body.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(8, 15).addBox(0.0F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(8, 1).addBox(0.0F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(5, 19).addBox(0.0F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.2771F, -1.3463F, -4.9556F, 0.0F, 2.8885F, 0.0F));

		PartDefinition cube_r26 = body.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(18, 7).addBox(-5.2512F, -0.85F, 3.4542F, 3.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6193F, 10.0037F, -0.2472F, -0.1191F, 0.7469F, -0.0811F));

		PartDefinition cube_r27 = body.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(0, 16).addBox(-5.0168F, -0.95F, 5.4536F, 4.5F, 1.9F, 2.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6193F, 10.0037F, -0.2472F, -0.0874F, 0.0522F, -0.0046F));

		PartDefinition cube_r28 = body.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(5, 13).addBox(-1.678F, -0.65F, 1.4759F, 3.1F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6193F, 10.0037F, -0.2472F, -1.1912F, 1.4768F, -1.1897F));

		PartDefinition cube_r29 = body.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(3, 14).addBox(-4.4075F, -0.75F, 1.7591F, 3.5F, 1.5F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6193F, 10.0037F, -0.2472F, -0.3362F, 1.3034F, -0.3251F));

		PartDefinition cube_r30 = body.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(25, 23).addBox(2.4699F, -0.65F, 5.1268F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6193F, 10.0037F, -0.2472F, -3.0515F, 0.2521F, -3.1191F));

		PartDefinition cube_r31 = body.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(14, 19).addBox(-1.7F, -2.0F, 0.0F, 1.7F, 1.7F, 2.1F, new CubeDeformation(0.0F))
				.texOffs(7, 18).addBox(-1.7F, 6.0F, 0.0F, 1.7F, 1.7F, 2.1F, new CubeDeformation(0.0F))
				.texOffs(13, 14).addBox(-1.7F, 2.0F, 0.0F, 1.7F, 1.7F, 2.1F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.6843F, -1.3463F, 4.0238F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r32 = body.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(14, 19).addBox(-1.3F, -2.0F, 0.0F, 1.3F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(17, 23).addBox(-1.3F, 6.0F, 0.0F, 1.3F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(14, 19).addBox(-1.3F, 2.0F, 0.0F, 1.3F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.778F, -1.3463F, -5.7159F, 0.0F, -3.098F, 0.0F));

		PartDefinition cube_r33 = body.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(8, 15).addBox(-2.5F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 28).addBox(-2.5F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(10, 18).addBox(-2.5F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.2006F, -1.3463F, -5.524F, 0.0F, -3.0631F, 0.0F));

		PartDefinition cube_r34 = body.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(8, 15).addBox(-2.5F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 28).addBox(-2.5F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 12).addBox(-2.5F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.2771F, -1.3463F, -4.9556F, 0.0F, -2.8885F, 0.0F));

		PartDefinition cube_r35 = body.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(8, 15).addBox(-2.5F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(12, 21).addBox(-2.5F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(4, 20).addBox(-2.5F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.4348F, -1.3463F, -3.7585F, 0.0F, -2.4522F, 0.0F));

		PartDefinition cube_r36 = body.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(8, 15).addBox(-2.5F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(15, 6).addBox(-2.5F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(14, 24).addBox(-2.5F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.1147F, -1.3463F, -2.1554F, 0.0F, -2.0595F, 0.0F));

		PartDefinition cube_r37 = body.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(8, 15).addBox(-2.5F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(2, 2).addBox(-2.5F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(15, 20).addBox(-2.5F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.3697F, -1.3463F, -0.2717F, 0.0F, -1.7541F, 0.0F));

		PartDefinition cube_r38 = body.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(8, 15).addBox(-2.5F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 12).addBox(-2.5F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(18, 23).addBox(-2.5F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.0464F, -1.3463F, 1.6014F, 0.0F, -1.4486F, 0.0F));

		PartDefinition cube_r39 = body.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(8, 15).addBox(-2.5F, -2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(1, 14).addBox(-2.5F, 6.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(23, 13).addBox(-2.5F, 2.0F, 0.0F, 2.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-9.388F, -1.3463F, 3.6512F, 0.0F, -1.2741F, 0.0F));

		PartDefinition cube_r40 = body.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(8, 8).addBox(-3.5F, -2.1F, -0.1F, 3.5F, 1.9F, 2.3F, new CubeDeformation(0.0F))
				.texOffs(6, 13).addBox(-3.5F, 5.9F, -0.1F, 3.5F, 1.9F, 2.3F, new CubeDeformation(0.0F))
				.texOffs(6, 12).addBox(-3.5F, 1.9F, -0.1F, 3.5F, 1.9F, 2.3F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.1318F, -1.3463F, 6.1557F, 0.0F, -0.6981F, 0.0F));

		PartDefinition cube_r41 = body.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(16, 13).addBox(-2.3584F, -0.85F, 7.2865F, 3.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 0.0477F, -0.7499F, -0.0325F));

		PartDefinition cube_r42 = body.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(21, 12).addBox(0.9813F, -0.65F, 7.3531F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 3.0979F, -0.6453F, -3.1153F));

		PartDefinition cube_r43 = body.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(3, 13).addBox(1.4704F, -0.65F, 6.5337F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 3.1055F, -0.2529F, -3.1326F));

		PartDefinition cube_r44 = body.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(23, 14).addBox(1.4608F, -0.65F, 6.9214F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 3.1032F, -0.4273F, -3.1257F));

		PartDefinition cube_r45 = body.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(21, 18).addBox(-0.8331F, -0.65F, 7.6438F, 2.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 3.0776F, -0.9939F, -3.0879F));

		PartDefinition cube_r46 = body.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(1, 15).addBox(-3.3283F, -0.65F, 7.1986F, 2.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 2.5532F, -1.5079F, -2.5541F));

		PartDefinition cube_r47 = body.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(21, 17).addBox(-0.4994F, -0.75F, 7.1994F, 3.5F, 1.5F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 0.1386F, -1.3154F, -0.1341F));

		PartDefinition cube_r48 = body.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(18, 12).addBox(-1.6181F, -0.65F, 7.0918F, 3.1F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 0.7857F, -1.5214F, -0.7851F));

		PartDefinition cube_r49 = body.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(10, 12).addBox(-6.5F, -2.2F, -0.2F, 6.5F, 2.1F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(1, 2).addBox(-6.5F, -6.2F, -0.2F, 6.5F, 2.1F, 2.4F, new CubeDeformation(0.0F))
				.texOffs(0, 19).addBox(-6.5F, -10.2F, -0.2F, 6.5F, 2.1F, 2.4F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.9F, 6.6537F, 5.9613F, 0.0F, -0.0524F, 0.0F));

		PartDefinition cube_r50 = body.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(18, 4).addBox(-5.0948F, -0.95F, 5.7477F, 4.5F, 1.9F, 2.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.9963F, 0.1528F, 0.035F, -0.0523F, -0.0018F));

		PartDefinition cube_r51 = body.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(1, 16).addBox(0.5168F, -0.95F, 5.4536F, 4.5F, 1.9F, 2.2F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6193F, 10.0037F, -0.2472F, -0.0874F, -0.0522F, 0.0046F));

		PartDefinition cube_r52 = body.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(12, 28).addBox(1.7512F, -0.85F, 3.4542F, 3.5F, 1.7F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6193F, 10.0037F, -0.2472F, -0.1191F, -0.7469F, 0.0811F));

		PartDefinition cube_r53 = body.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(4, 0).addBox(0.9075F, -0.75F, 1.7591F, 3.5F, 1.5F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6193F, 10.0037F, -0.2472F, -0.3362F, -1.3034F, 0.3251F));

		PartDefinition cube_r54 = body.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(16, 0).addBox(-1.422F, -0.65F, 1.4759F, 3.1F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6193F, 10.0037F, -0.2472F, -1.1912F, -1.4768F, 1.1897F));

		PartDefinition cube_r55 = body.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(23, 8).addBox(-3.6224F, -0.65F, 1.587F, 2.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6193F, 10.0037F, -0.2472F, -2.1099F, -1.4691F, 2.1122F));

		PartDefinition cube_r56 = body.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(22, 22).addBox(-3.8936F, -0.65F, 2.931F, 2.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6193F, 10.0037F, -0.2472F, -2.9823F, -0.989F, 3.0082F));

		PartDefinition cube_r57 = body.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(25, 26).addBox(-3.5064F, -0.65F, 3.9714F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6193F, 10.0037F, -0.2472F, -3.0325F, -0.6429F, 3.076F));

		PartDefinition cube_r58 = body.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(9, 15).addBox(-3.6525F, -0.65F, 4.5911F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6193F, 10.0037F, -0.2472F, -3.0457F, -0.4259F, 3.1019F));

		PartDefinition cube_r59 = body.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(1, 6).addBox(-3.9699F, -0.65F, 5.1268F, 1.5F, 1.3F, 1.9F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6193F, 10.0037F, -0.2472F, -3.0515F, -0.2521F, 3.1191F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}