package mod.vemerion.minecard.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.ClientState;
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
	ClientState state;
	private List<ClientCard> enemyHand;
	private List<ClientCard> yourHand;
	private List<ClientCard> enemyBoard;
	private List<ClientCard> yourBoard;
	private UUID current = UUID.randomUUID();

	// Text
	TurnText turnText;

	public GameScreen(ClientState state) {
		super(TITLE);
		this.state = state;
		this.turnText = new TurnText();
		updateState();
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
		enemyHand = new ArrayList<>();
		yourHand = new ArrayList<>();
		enemyBoard = new ArrayList<>();
		yourBoard = new ArrayList<>();

		for (int i = 0; i < state.enemyHand; i++) {
			enemyHand.add(new ClientCard(Cards.EMPTY, new Vec2(cardRowX(state.enemyHand, state.enemyHand - i - 1), 5)));
		}

		for (int i = 0; i < state.yourHand.size(); i++) {
			yourHand.add(new ClientCard(state.yourHand.get(i),
					new Vec2(cardRowX(state.yourHand.size(), i), height - CARD_HEIGHT - 5)));
		}

		for (int i = 0; i < state.enemyBoard.size(); i++) {
			enemyBoard.add(new ClientCard(state.enemyBoard.get(i),
					new Vec2(cardRowX(state.enemyBoard.size(), state.enemyBoard.size() - i - 1), 150)));
		}

		for (int i = 0; i < state.yourBoard.size(); i++) {
			yourBoard.add(new ClientCard(state.yourBoard.get(i),
					new Vec2(cardRowX(state.yourBoard.size(), i), height - CARD_HEIGHT - 150)));
		}
	}

	private int cardRowX(int total, int i) {
		return (width - (total - 1) * CARD_WIDTH) / 2 + i * CARD_WIDTH - CARD_WIDTH / 2;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		var source = Minecraft.getInstance().renderBuffers().bufferSource();

		for (var card : enemyHand)
			card.render(mouseX, mouseY, source);
		for (var card : yourHand)
			card.render(mouseX, mouseY, source);
		for (var card : enemyBoard)
			card.render(mouseX, mouseY, source);
		for (var card : yourBoard)
			card.render(mouseX, mouseY, source);

		// Decks
		for (int i = 0; i < state.enemyDeck; i++) {
			new ClientCard(Cards.EMPTY, new Vec2(20 + i * 0.2f, 20)).render(mouseX, mouseY, source);
		}
		for (int i = 0; i < state.yourDeck; i++) {
			new ClientCard(Cards.EMPTY, new Vec2(width - 80 + i * 0.2f, height - 80)).render(mouseX, mouseY, source);
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

	private static class ClientCard {

		private Card card;
		private Vec2 position;

		public ClientCard(Card card, Vec2 position) {
			this.card = card;
			this.position = position;
		}

		public void render(int mouseX, int mouseY, BufferSource source) {
			var ps = new PoseStack();
			ps.pushPose();

			// Rotate to show back
			if (card.getType() == null) {
				ps.translate(position.x + 24, 0, 0);
				ps.mulPose(new Quaternion(0, 180, 0, true));
				ps.translate(-position.x - 24, 0, 0);
			}

			ps.translate(position.x, position.y, 0);
			ps.scale(CARD_SCALE, -CARD_SCALE, CARD_SCALE);
			int light = contains(mouseX, mouseY) ? CARD_LIGHT : CARD_LIGHT_HOVER;
			CardItemRenderer.renderCard(card, TransformType.NONE, ps, source, light, OverlayTexture.NO_OVERLAY);
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
