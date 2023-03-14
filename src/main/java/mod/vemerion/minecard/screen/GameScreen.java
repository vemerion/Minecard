package mod.vemerion.minecard.screen;

import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.ClientPlayerState;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.renderer.CardItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec2;

public class GameScreen extends Screen {

	public static final Component TITLE = new TranslatableComponent("gui." + Main.MODID + ".game");

	private static final int CARD_SCALE = 60;
	private static final int CARD_LIGHT = LightTexture.FULL_BRIGHT;
	private static final int CARD_LIGHT_HOVER = 0b011000000000000001100000;
	private static final int CARD_WIDTH = 46;
	private static final int CARD_HEIGHT = 48;

	// State
	List<ClientPlayerState> state;
	private UUID current = UUID.randomUUID();

	// Text
	TurnText turnText;

	public GameScreen(List<ClientPlayerState> state) {
		super(TITLE);
		this.state = state;
		this.turnText = new TurnText();
	}

	@Override
	protected void init() {
		super.init();
		updateState();
	}

	public void setCurrent(UUID current) {
		this.current = current;
		turnText.change(current.equals(minecraft.player.getUUID()));
	}

	private void updateState() {
		for (var playerState : state) {
			boolean enemy = !playerState.id.equals(minecraft.player.getUUID());
			for (int i = 0; i < playerState.hand.size(); i++) {
				int x = cardRowX(playerState.hand.size(), enemy ? playerState.hand.size() - i - 1 : i);
				int y = enemy ? 5 : height - CARD_HEIGHT - 5;
				playerState.hand.set(i, new ClientCard(playerState.hand.get(i), new Vec2(x, y)));
			}
			for (int i = 0; i < playerState.board.size(); i++) {
				int x = cardRowX(playerState.board.size(), enemy ? playerState.board.size() - i - 1 : i);
				int y = enemy ? 150 : height - CARD_HEIGHT - 150;
				playerState.board.set(i, new ClientCard(playerState.board.get(i), new Vec2(x, y)));
			}
		}
	}

	private int cardRowX(int total, int i) {
		return (width - (total - 1) * CARD_WIDTH) / 2 + i * CARD_WIDTH - CARD_WIDTH / 2;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		var source = Minecraft.getInstance().renderBuffers().bufferSource();

		for (var playerState : state) {
			boolean enemy = !playerState.id.equals(minecraft.player.getUUID());
			for (var card : playerState.board)
				((ClientCard) card).render(mouseX, mouseY, source);
			for (var card : playerState.hand)
				((ClientCard) card).render(mouseX, mouseY, source);

			for (int i = 0; i < playerState.deck; i++) {
				float x = enemy ? 20 + i * 0.2f : width - 80 + i * 0.2f;
				float y = enemy ? 20 : height - 80;
				new ClientCard(Cards.EMPTY, new Vec2(x, y)).render(mouseX, mouseY, source);
			}
		}

		source.endBatch();

		turnText.render(poseStack, mouseX, mouseY, partialTicks);

		super.render(poseStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void tick() {
		super.tick();

		turnText.tick();
	}

	private static class ClientCard extends Card {

		private Vec2 position;

		public ClientCard(Card card, Vec2 position) {
			super(card.getType(), card.getCost(), card.getHealth(), card.getDamage());
			this.position = position;
		}

		public void render(int mouseX, int mouseY, BufferSource source) {
			var ps = new PoseStack();
			ps.pushPose();

			// Rotate to show back
			if (getType() == null) {
				ps.translate(position.x + 24, 0, 0);
				ps.mulPose(new Quaternion(0, 180, 0, true));
				ps.translate(-position.x - 24, 0, 0);
			}

			ps.translate(position.x, position.y, 0);
			ps.scale(CARD_SCALE, -CARD_SCALE, CARD_SCALE);
			int light = contains(mouseX, mouseY) ? CARD_LIGHT : CARD_LIGHT_HOVER;
			CardItemRenderer.renderCard(this, TransformType.NONE, ps, source, light, OverlayTexture.NO_OVERLAY);
			ps.popPose();
		}

		private boolean contains(int x, int y) {
			return x > position.x && x < position.x + CARD_WIDTH && y > position.y && y < position.y + CARD_HEIGHT;
		}

	}

	private class TurnText implements Widget {

		private static Component YOUR_TURN = new TranslatableComponent(Helper.gui("your_turn"));
		private static Component ENEMY_TURN = new TranslatableComponent(Helper.gui("enemy_turn"));

		private Component text;
		private int alpha;
		private float scale;

		private TurnText() {
			this.text = YOUR_TURN;
		}

		@Override
		public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
			var font = minecraft.font;
			pPoseStack.pushPose();
			pPoseStack.translate(width / 2 - font.width(text) * scale / 2, height / 2 - font.lineHeight / 2 * scale, 0);
			pPoseStack.scale(scale, scale, scale);
			font.draw(pPoseStack, text, 0, 0, FastColor.ARGB32.color(alpha, 255, 255, 0));
			pPoseStack.popPose();
		}

		private void tick() {
			alpha = (int) (alpha * 0.95);
			scale = scale * 0.95f;
		}

		private void change(boolean yourTurn) {
			text = yourTurn ? YOUR_TURN : ENEMY_TURN;
			alpha = 255;
			scale = 3;
		}

	}
}
