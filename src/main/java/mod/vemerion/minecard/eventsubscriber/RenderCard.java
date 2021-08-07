package mod.vemerion.minecard.eventsubscriber;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Main.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class RenderCard {

	@SubscribeEvent
	public static void renderCard(RenderHandEvent event) {
		ItemStack stack = event.getItemStack();

		if (stack.getItem() != Main.CARD)
			return;
		event.setCanceled(true);

		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		boolean bothArms = event.getHand() == InteractionHand.MAIN_HAND && player.getOffhandItem().isEmpty();
		HumanoidArm arm = event.getHand() == InteractionHand.MAIN_HAND ? player.getMainArm()
				: player.getMainArm().getOpposite();
		float offset = bothArms ? 0 : (arm == HumanoidArm.LEFT ? -1 : 1);
		float xProgress = bothArms ? Mth.clamp(player.getXRot(), 0, 30) / 30 : 1;
		PlayerRenderer renderer = (PlayerRenderer) mc.getEntityRenderDispatcher().getRenderer(player);
		PoseStack pose = event.getMatrixStack();
		pose.pushPose();
		pose.translate(0,
				-0.4 + 0.3 * xProgress - getEquipProgress(event)
						- (bothArms ? Mth.sin(getSwingProgress(player, event) * Mth.PI) : 0),
				-0.7 - (bothArms ? Mth.sin(getSwingProgress(player, event) * Mth.PI) : 0));

		// Render hand(s)
		pose.pushPose();
		pose.translate(offset, -0.8, 0.4);
		pose.mulPose(new Quaternion(-50, 0, offset * Mth.sin(getSwingProgress(player, event)) * 80, true));
		renderArm(arm, renderer, pose, event.getBuffers(), event.getLight(), player);
		if (bothArms)
			renderArm(arm.getOpposite(), renderer, pose, event.getBuffers(), event.getLight(), player);
		pose.popPose();

		// Render card
		pose.translate(offset * 0.6 - offset * Mth.sin(getSwingProgress(player, event)),
				bothArms ? 0 : -Mth.sin(getSwingProgress(player, event)) * 2, 0);
		pose.scale(0.5f, 0.5f, 0.5f);
		pose.mulPose(new Quaternion(80 * xProgress - 90, 0, 0, true));
		Minecraft.getInstance().getItemRenderer().renderStatic(null, stack, TransformType.NONE, false,
				event.getMatrixStack(), event.getBuffers(), null, event.getLight(), OverlayTexture.NO_OVERLAY, 0);

		// Render entity
		pose.pushPose();
		pose.translate(0, -0.4, 0);
		pose.scale(0.4f, 0.4f, 0.4f);
		pose.mulPose(new Quaternion(0, player.tickCount + event.getPartialTicks(), 0, true));
		((EntityRenderer) Minecraft.getInstance().getEntityRenderDispatcher().renderers.get(EntityType.ENDERMAN))
				.render(EntityType.ENDERMAN.create(Minecraft.getInstance().level), 0, 0, pose, event.getBuffers(),
						event.getLight());

		pose.popPose();
		pose.popPose();
//		ZombieModel<?> model = new ZombieModel<>(Minecraft.getInstance().);
//		model.renderToBuffer(pose,
//				event.getBuffers()
//						.getBuffer(RenderType.entityCutout(new ResourceLocation("textures/entity/zombie/zombie.png"))),
//				OverlayTexture.NO_OVERLAY, event.getLight(), 1, 1, 1, 1);
	}

	private static void renderArm(HumanoidArm arm, PlayerRenderer renderer, PoseStack pose, MultiBufferSource buffers,
			int light, LocalPlayer player) {
		float offset = arm == HumanoidArm.LEFT ? -1 : 1;
		pose.pushPose();
		pose.translate(-offset * 0.3, 0, 0);
		pose.mulPose(new Quaternion(0, 0, -offset * 35, true));
		if (arm == HumanoidArm.LEFT)
			renderer.renderLeftHand(pose, buffers, light, player);
		else if (arm == HumanoidArm.RIGHT)
			renderer.renderRightHand(pose, buffers, light, player);
		pose.popPose();
	}

	// TODO: Change this when Forge bug is fixed
	private static float getEquipProgress(RenderHandEvent event) {
		if (event.getHand() == InteractionHand.MAIN_HAND)
			return 0;
		return event.getEquipProgress();
	}

	// TODO: Change this when Forge bug is fixed
	private static float getSwingProgress(Player player, RenderHandEvent event) {
		if (event.getHand() == InteractionHand.OFF_HAND)
			return event.getSwingProgress();
		return player.swingingArm == event.getHand() ? player.getAttackAnim(event.getPartialTicks()) : 0;
	}
}
