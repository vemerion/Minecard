package mod.vemerion.minecard.renderer;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.entity.CardGameRobot;
import mod.vemerion.minecard.model.CardGameRobotModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class CardGameRobotRenderer extends MobRenderer<CardGameRobot, CardGameRobotModel> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID,
			"textures/entity/card_game_robot.png");

	public CardGameRobotRenderer(EntityRendererProvider.Context context) {
		super(context, new CardGameRobotModel(context.bakeLayer(CardGameRobotModel.LAYER)), 0);
	}

	@Override
	public ResourceLocation getTextureLocation(CardGameRobot entity) {
		return TEXTURE;
	}

}
