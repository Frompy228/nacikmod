package net.artur.nacikmod.entity.client;// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


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

public class ProjectileManaSwordModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			new ResourceLocation(NacikMod.MOD_ID, "mana_sword_projectile"), "main");
	private final ModelPart bb_main;

	public ProjectileManaSwordModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 22).addBox(-7.5F, -0.5F, 5.75F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 16).addBox(-6.5F, -0.5F, 5.75F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(6, 21).addBox(-4.5F, -0.5F, 4.75F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(20, 20).addBox(-3.5F, -0.5F, 3.75F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(14, 20).addBox(-2.5F, -0.5F, 2.75F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 15).addBox(-7.5F, -0.5F, 4.75F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(16, 13).addBox(-6.5F, -0.5F, 3.75F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(16, 11).addBox(-5.5F, -0.5F, 2.75F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(16, 9).addBox(-4.5F, -0.5F, 1.75F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 16).addBox(-3.5F, -0.5F, 0.75F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(16, 7).addBox(-2.5F, -0.5F, -0.25F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(16, 5).addBox(-1.5F, -0.5F, -1.25F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(20, 0).addBox(-1.5F, -0.5F, 1.75F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 19).addBox(-0.5F, -0.5F, 0.75F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(8, 18).addBox(0.5F, -0.5F, -0.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 17).addBox(1.5F, -0.5F, -1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 3).addBox(-0.5F, -0.5F, -2.25F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(8, 13).addBox(2.5F, -0.5F, -2.25F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 13).addBox(2.5F, -0.5F, -4.25F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(12, 0).addBox(1.5F, -0.5F, -6.25F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(8, 10).addBox(5.5F, -0.5F, -2.25F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(1, 25).addBox(0.5F, -0.5F, -4.25F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 28).addBox(0.5F, -0.5F, -7.25F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(8, 4).addBox(4.5F, -0.5F, -3.25F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 7).addBox(3.5F, -0.5F, -5.25F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 4).addBox(4.5F, -0.5F, -6.25F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(5.5F, -0.5F, -8.25F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -8.5F, 1.25F, 0.0F, -2.3562F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}