// Made with Blockbench 4.12.5
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports
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
import net.artur.nacikmod.NacikMod;

public class DoubleSlashProjectileModel<T extends Entity> extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation(NacikMod.MOD_ID, "double_slash_projectile"), "main");
	private final ModelPart bb_main;

	public DoubleSlashProjectileModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(20, 20).mirror().addBox(1.9518F, 0.0036F, -19.8021F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, 0.0F, -1.3963F, 0.7854F));

		PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(20, 24).mirror().addBox(11.2434F, 0.0036F, -2.3714F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 20).mirror().addBox(11.261F, -0.9964F, -3.9992F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, 0.0F, -0.1745F, 0.7854F));

		PartDefinition cube_r3 = bb_main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(20, 16).mirror().addBox(3.5971F, 0.0036F, -1.9029F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 12).mirror().addBox(3.3883F, -0.9964F, -3.4892F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, 0.0F, -0.1309F, 0.7854F));

		PartDefinition cube_r4 = bb_main.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(11.2496F, 1.4784F, -3.4834F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, -0.7854F, -0.1745F, 0.7854F));

		PartDefinition cube_r5 = bb_main.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(20, 8).mirror().addBox(3.4169F, 1.1875F, -3.1925F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, -0.7854F, -0.1309F, 0.7854F));

		PartDefinition cube_r6 = bb_main.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(20, 22).mirror().addBox(-9.9518F, 0.0036F, -19.8021F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, 0.0F, 1.3963F, 0.7854F));

		PartDefinition cube_r7 = bb_main.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(0, 26).mirror().addBox(-19.2434F, 0.0036F, -2.3714F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(20, 0).mirror().addBox(-19.261F, -0.9964F, -3.9992F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, 0.0F, 0.1745F, 0.7854F));

		PartDefinition cube_r8 = bb_main.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(20, 18).mirror().addBox(-11.5971F, 0.0036F, -1.9029F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 16).mirror().addBox(-11.3883F, -0.9964F, -3.4892F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, 0.0F, 0.1309F, 0.7854F));

		PartDefinition cube_r9 = bb_main.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 24).mirror().addBox(-3.95F, 0.0036F, -1.4036F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 8).mirror().addBox(-3.95F, -0.9964F, -3.0036F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, 0.0F, 0.0F, 0.7854F));

		PartDefinition cube_r10 = bb_main.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(0, 4).mirror().addBox(-19.2496F, 1.4784F, -3.4834F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, -0.7854F, 0.1745F, 0.7854F));

		PartDefinition cube_r11 = bb_main.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(20, 12).mirror().addBox(-11.4169F, 1.1875F, -3.1925F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, -0.7854F, 0.1309F, 0.7854F));

		PartDefinition cube_r12 = bb_main.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(20, 4).mirror().addBox(-3.95F, 0.8335F, -2.8385F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -7.0036F, 2.0036F, -0.7854F, 0.0F, 0.7854F));

		PartDefinition cube_r13 = bb_main.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(20, 20).addBox(-9.9518F, 0.0036F, -19.8021F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, 0.0F, 1.3963F, -0.7854F));

		PartDefinition cube_r14 = bb_main.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(20, 24).addBox(-19.2434F, 0.0036F, -2.3714F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 20).addBox(-19.261F, -0.9964F, -3.9992F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, 0.0F, 0.1745F, -0.7854F));

		PartDefinition cube_r15 = bb_main.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(20, 16).addBox(-11.5971F, 0.0036F, -1.9029F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 12).addBox(-11.3883F, -0.9964F, -3.4892F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, 0.0F, 0.1309F, -0.7854F));

		PartDefinition cube_r16 = bb_main.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(0, 0).addBox(-19.2496F, 1.4784F, -3.4834F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, -0.7854F, 0.1745F, -0.7854F));

		PartDefinition cube_r17 = bb_main.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(20, 8).addBox(-11.4169F, 1.1875F, -3.1925F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, -0.7854F, 0.1309F, -0.7854F));

		PartDefinition cube_r18 = bb_main.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(20, 22).addBox(1.9518F, 0.0036F, -19.8021F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, 0.0F, -1.3963F, -0.7854F));

		PartDefinition cube_r19 = bb_main.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(0, 26).addBox(11.2434F, 0.0036F, -2.3714F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(20, 0).addBox(11.261F, -0.9964F, -3.9992F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, 0.0F, -0.1745F, -0.7854F));

		PartDefinition cube_r20 = bb_main.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(20, 18).addBox(3.5971F, 0.0036F, -1.9029F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 16).addBox(3.3883F, -0.9964F, -3.4892F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, 0.0F, -0.1309F, -0.7854F));

		PartDefinition cube_r21 = bb_main.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(0, 24).addBox(-4.05F, 0.0036F, -1.4036F, 8.0F, 0.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 8).addBox(-4.05F, -0.9964F, -3.0036F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, 0.0F, 0.0F, -0.7854F));

		PartDefinition cube_r22 = bb_main.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(0, 4).addBox(11.2496F, 1.4784F, -3.4834F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, -0.7854F, -0.1745F, -0.7854F));

		PartDefinition cube_r23 = bb_main.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(20, 12).addBox(3.4169F, 1.1875F, -3.1925F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, -0.7854F, -0.1309F, -0.7854F));

		PartDefinition cube_r24 = bb_main.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(20, 4).addBox(-4.05F, 0.8335F, -2.8385F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -7.0036F, 2.0036F, -0.7854F, 0.0F, -0.7854F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}