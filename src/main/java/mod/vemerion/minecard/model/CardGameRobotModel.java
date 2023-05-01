package mod.vemerion.minecard.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.entity.CardGameRobot;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

// Made with Blockbench 4.4.2

public class CardGameRobotModel extends EntityModel<CardGameRobot> {
	public static final ModelLayerLocation LAYER = new ModelLayerLocation(
			new ResourceLocation(Main.MODID, "card_game_robot"), "main");
	private final ModelPart base;

	public CardGameRobotModel(ModelPart root) {
		this.base = root.getChild("base");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition base = partdefinition.addOrReplaceChild("base",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-6.0F, -19.0F, -5.0F, 12.0F, 12.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(16, 38)
						.addBox(-2.0F, -22.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 23.0F, 0.0F));

		base.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 22)
						.addBox(-5.0F, -8.0F, -4.0F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(44, 44)
						.addBox(-2.0F, -14.0F, 0.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -22.0F, 0.0F));

		PartDefinition leftArm1 = base.addOrReplaceChild("leftArm1", CubeListBuilder.create().texOffs(44, 10).addBox(
				0.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(6.0F, -17.0F, 0.0F));

		PartDefinition leftArm2 = leftArm1.addOrReplaceChild("leftArm2", CubeListBuilder.create().texOffs(0, 0).addBox(
				-1.0F, 0.0F, -2.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 1.0F, 1.0F));

		leftArm2.addOrReplaceChild("leftArm3", CubeListBuilder.create().texOffs(32, 44).addBox(-3.0F, 0.0F, -2.0F, 6.0F,
				6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 1.0F));

		PartDefinition rightArm1 = base.addOrReplaceChild("rightArm1",
				CubeListBuilder.create().texOffs(44, 10).mirror()
						.addBox(-6.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-6.0F, -17.0F, 0.0F));

		PartDefinition rightArm2 = rightArm1.addOrReplaceChild("rightArm2", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-5.0F, 1.0F, 0.0F));

		rightArm2.addOrReplaceChild("rightArm3", CubeListBuilder.create().texOffs(32, 44).addBox(-3.0F, 3.0F, -2.0F,
				6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 2.0F));

		PartDefinition leftLeg1 = base.addOrReplaceChild("leftLeg1", CubeListBuilder.create().texOffs(34, 0).addBox(
				-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, -7.0F, 0.0F));

		leftLeg1.addOrReplaceChild("leftLeg2", CubeListBuilder.create().texOffs(28, 22).addBox(-3.0F, 0.0F, -3.0F, 6.0F,
				2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition rightLeg1 = base.addOrReplaceChild("rightLeg1", CubeListBuilder.create().texOffs(34, 0).addBox(
				-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, -7.0F, 0.0F));

		rightLeg1.addOrReplaceChild("rightLeg2", CubeListBuilder.create().texOffs(28, 22).addBox(-3.0F, 0.0F, -3.0F,
				6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(CardGameRobot entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		base.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}