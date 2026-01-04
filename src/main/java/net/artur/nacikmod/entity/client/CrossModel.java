package net.artur.nacikmod.entity.client;

import net.artur.nacikmod.NacikMod;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

// Made with Blockbench 5.0.4
// Exported for Minecraft version 1.17 or later with Mojang mappings

public class CrossModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(NacikMod.MOD_ID, "cross_projectile"), "main");
	private final ModelPart bone;
	private final ModelPart bb_main;
	private final ModelPart bone_outline;
	private final ModelPart bb_main_outline;

	public CrossModel(ModelPart root) {
		this.bone = root.getChild("bone");
		this.bb_main = root.getChild("bb_main");
		this.bone_outline = root.getChild("bone_outline");
		this.bb_main_outline = root.getChild("bb_main_outline");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(80, 62).addBox(14.0F, -20.0F, -11.0F, 10.0F, 52.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(80, 62).addBox(14.0F, -20.0F, 0.0F, 10.0F, 52.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(73, 51).addBox(24.0F, -1.0F, -11.0F, 1.0F, 33.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(83, 61).mirror().addBox(13.0F, -1.0F, -11.0F, 1.0F, 33.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(68, 51).addBox(13.0F, -21.0F, -11.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(74, 51).addBox(13.0F, -20.0F, -11.0F, 1.0F, 11.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(74, 51).mirror().addBox(24.0F, -20.0F, -11.0F, 1.0F, 11.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(85, 62).addBox(24.0F, -9.0F, -11.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(85, 62).addBox(24.0F, -9.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(85, 62).mirror().addBox(13.0F, -9.0F, -11.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(85, 62).mirror().addBox(13.0F, -9.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(60, 51).addBox(-2.0F, -10.0F, -11.0F, 15.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(60, 51).addBox(-2.0F, -1.0F, -11.0F, 15.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(59, 51).mirror().addBox(25.0F, -1.0F, -11.0F, 15.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(59, 51).mirror().addBox(25.0F, -10.0F, -11.0F, 15.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(74, 51).addBox(-2.0F, -9.0F, -11.0F, 1.0F, 8.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(83, 61).mirror().addBox(39.0F, -9.0F, -11.0F, 1.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(84, 62).addBox(-1.0F, -9.0F, -11.0F, 14.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(84, 62).addBox(-1.0F, -9.0F, 0.0F, 14.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(83, 61).mirror().addBox(25.0F, -9.0F, 0.0F, 14.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(84, 62).mirror().addBox(25.0F, -9.0F, -11.0F, 14.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(82, 113).addBox(13.0F, 32.0F, -11.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(-19.0F, -9.0F, 5.0F));

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -53.0F, -5.0F, 10.0F, 52.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(40, 0).addBox(-20.0F, -42.0F, -5.0F, 15.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(40, 18).addBox(5.0F, -42.0F, -5.0F, 15.0F, 8.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		// Внешний слой для второй текстуры (немного больше)
		PartDefinition bone_outline = partdefinition.addOrReplaceChild("bone_outline", CubeListBuilder.create().texOffs(80, 62).addBox(14.0F, -20.0F, -11.0F, 10.0F, 52.0F, 1.0F, new CubeDeformation(0.1F))
		.texOffs(80, 62).addBox(14.0F, -20.0F, 0.0F, 10.0F, 52.0F, 1.0F, new CubeDeformation(0.1F))
		.texOffs(73, 51).addBox(24.0F, -1.0F, -11.0F, 1.0F, 33.0F, 12.0F, new CubeDeformation(0.1F))
		.texOffs(83, 61).mirror().addBox(13.0F, -1.0F, -11.0F, 1.0F, 33.0F, 12.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(68, 51).addBox(13.0F, -21.0F, -11.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.1F))
		.texOffs(74, 51).addBox(13.0F, -20.0F, -11.0F, 1.0F, 11.0F, 12.0F, new CubeDeformation(0.1F))
		.texOffs(74, 51).mirror().addBox(24.0F, -20.0F, -11.0F, 1.0F, 11.0F, 12.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(85, 62).addBox(24.0F, -9.0F, -11.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.1F))
		.texOffs(85, 62).addBox(24.0F, -9.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.1F))
		.texOffs(85, 62).mirror().addBox(13.0F, -9.0F, -11.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(85, 62).mirror().addBox(13.0F, -9.0F, 0.0F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(60, 51).addBox(-2.0F, -10.0F, -11.0F, 15.0F, 1.0F, 12.0F, new CubeDeformation(0.1F))
		.texOffs(60, 51).addBox(-2.0F, -1.0F, -11.0F, 15.0F, 1.0F, 12.0F, new CubeDeformation(0.1F))
		.texOffs(59, 51).mirror().addBox(25.0F, -1.0F, -11.0F, 15.0F, 1.0F, 12.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(59, 51).mirror().addBox(25.0F, -10.0F, -11.0F, 15.0F, 1.0F, 12.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(74, 51).addBox(-2.0F, -9.0F, -11.0F, 1.0F, 8.0F, 12.0F, new CubeDeformation(0.1F))
		.texOffs(83, 61).mirror().addBox(39.0F, -9.0F, -11.0F, 1.0F, 8.0F, 12.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(84, 62).addBox(-1.0F, -9.0F, -11.0F, 14.0F, 8.0F, 1.0F, new CubeDeformation(0.1F))
		.texOffs(84, 62).addBox(-1.0F, -9.0F, 0.0F, 14.0F, 8.0F, 1.0F, new CubeDeformation(0.1F))
		.texOffs(83, 61).mirror().addBox(25.0F, -9.0F, 0.0F, 14.0F, 8.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(84, 62).mirror().addBox(25.0F, -9.0F, -11.0F, 14.0F, 8.0F, 1.0F, new CubeDeformation(0.1F)).mirror(false)
		.texOffs(82, 113).addBox(13.0F, 32.0F, -11.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.1F)), PartPose.offset(-19.0F, -9.0F, 5.0F));

		PartDefinition bb_main_outline = partdefinition.addOrReplaceChild("bb_main_outline", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -53.0F, -5.0F, 10.0F, 52.0F, 10.0F, new CubeDeformation(0.1F))
		.texOffs(40, 0).addBox(-20.0F, -42.0F, -5.0F, 15.0F, 8.0F, 10.0F, new CubeDeformation(0.1F))
		.texOffs(40, 18).addBox(5.0F, -42.0F, -5.0F, 15.0F, 8.0F, 10.0F, new CubeDeformation(0.1F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void renderOutline(com.mojang.blaze3d.vertex.PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone_outline.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		bb_main_outline.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}