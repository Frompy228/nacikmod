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


public class GraalModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "custommodel"), "main");
	private final ModelPart bb_main;

	public GraalModel(ModelPart root) {
		this.bb_main = root.getChild("bb_main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bb_main = partdefinition.addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(40, 6).addBox(3.0F, -11.0F, -5.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 21).addBox(-4.0F, -1.5F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(46, 30).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 57).addBox(-1.5F, -9.0F, -1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(0, 52).addBox(-2.0F, -7.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(48, 54).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(46, 41).addBox(-2.0F, -10.5F, -4.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 46).addBox(-2.5F, -10.0F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(46, 36).addBox(-4.0F, -12.0F, -5.5F, 8.0F, 1.0F, 1.5F, new CubeDeformation(0.0F))
				.texOffs(32, 21).addBox(-6.0F, -17.0F, -6.0F, 12.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(24, 30).addBox(5.0F, -17.0F, -5.0F, 1.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 30).addBox(-6.0F, -17.0F, -5.0F, 1.0F, 5.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(40, 0).addBox(-5.0F, -17.0F, 5.0F, 11.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(42, 63).addBox(-4.0F, -12.0F, 4.0F, 8.0F, 1.0F, 1.5F, new CubeDeformation(0.0F))
				.texOffs(24, 45).addBox(-5.0F, -11.0F, -5.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(24, 57).addBox(-5.0F, -11.0F, 3.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(40, 18).addBox(-3.0F, -11.0F, 3.0F, 8.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(16, 54).addBox(-3.0F, -11.0F, -5.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(44, 45).addBox(-4.0F, -10.5F, -4.0F, 2.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(32, 54).addBox(-2.0F, -10.5F, 2.0F, 6.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(12, 57).addBox(2.0F, -10.5F, -2.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 11).addBox(-5.0F, -16.0F, -5.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition cube_r1 = bb_main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(32, 27).addBox(-6.0F, -1.0F, -5.0F, 11.0F, 1.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(9.0F, -11.0F, -0.5F, 0.0F, 1.5708F, 0.0F));

		PartDefinition cube_r2 = bb_main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(40, 15).addBox(-6.0F, -1.0F, -5.0F, 11.0F, 1.0F, 1.5F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -11.0F, -0.5F, 0.0F, 1.5708F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bb_main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}