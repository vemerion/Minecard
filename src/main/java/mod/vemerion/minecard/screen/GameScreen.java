package mod.vemerion.minecard.screen;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.game.ClientState;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class GameScreen extends Screen {

	public static final Component TITLE = new TranslatableComponent("gui." + Main.MODID + ".game");

	private static final int CARD_SCALE = 60;
	private static final int YOUR_BOARD_BOTTOM_OFFSET = 70;
	private static final int CARD_Z = 1000;

	private ClientState state;

	public GameScreen(ClientState state) {
		super(TITLE);
		this.state = state;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		var source = Minecraft.getInstance().renderBuffers().bufferSource();

		// Your board
		int x = (width - (state.yourHand.size() - 1) * CARD_SCALE) / 2;
		for (var card : state.yourHand) {
			var stack = ModItems.CARD.get().getDefaultInstance();
			CardData.get(stack).ifPresent(data -> data.setType(card.getType()));
			var ps = new PoseStack();
			ps.pushPose();
			ps.translate(x, height - YOUR_BOARD_BOTTOM_OFFSET, CARD_Z);
			ps.scale(CARD_SCALE, -CARD_SCALE, CARD_SCALE);
			minecraft.getItemRenderer().renderStatic(null, stack, TransformType.NONE, false, ps, source, null,
					LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
			ps.popPose();
			x += CARD_SCALE;
		}
		source.endBatch();

		super.render(poseStack, mouseX, mouseY, partialTicks);
	}
}
