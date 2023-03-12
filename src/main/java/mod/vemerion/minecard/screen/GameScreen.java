package mod.vemerion.minecard.screen;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.ClientState;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.phys.Vec2;

public class GameScreen extends Screen {

	public static final Component TITLE = new TranslatableComponent("gui." + Main.MODID + ".game");

	private static final int CARD_SCALE = 60;
	private static final int YOUR_BOARD_BOTTOM_OFFSET = 25;
	private static final int CARD_Z = 1000;
	private static final int CARD_LIGHT = LightTexture.FULL_BRIGHT;
	private static final int CARD_LIGHT_HOVER = 0b011000000000000001100000;
	private static final int CARD_WIDTH = 44;
	private static final int CARD_HEIGHT = 50;

	// State
	ClientState state;
	private List<ClientCard> enemyHand;
	private List<ClientCard> yourHand;
	private List<ClientCard> enemyBoard;
	private List<ClientCard> yourBoard;

	public GameScreen(ClientState state) {
		super(TITLE);
		this.state = state;
		updateState();
	}

	@Override
	protected void init() {
		super.init();
		updateState();
	}

	private void updateState() {
		enemyHand = new ArrayList<>();
		yourHand = new ArrayList<>();
		enemyBoard = new ArrayList<>();
		yourBoard = new ArrayList<>();

		for (int i = 0; i < state.enemyHand; i++) {
			enemyHand
					.add(new ClientCard(Cards.EMPTY, new Vec2(cardRowX(state.enemyHand, state.enemyHand - i - 1), 35)));
		}

		for (int i = 0; i < state.yourHand.size(); i++) {
			yourHand.add(new ClientCard(state.yourHand.get(i),
					new Vec2(cardRowX(state.yourHand.size(), i), height - YOUR_BOARD_BOTTOM_OFFSET)));
		}

		for (int i = 0; i < state.enemyBoard.size(); i++) {
			enemyBoard.add(new ClientCard(state.enemyBoard.get(i),
					new Vec2(cardRowX(state.enemyBoard.size(), state.enemyBoard.size() - i - 1), 150)));
		}

		for (int i = 0; i < state.yourBoard.size(); i++) {
			yourBoard.add(new ClientCard(state.yourBoard.get(i),
					new Vec2(cardRowX(state.yourBoard.size(), i), height - YOUR_BOARD_BOTTOM_OFFSET - 100)));
		}
	}

	private int cardRowX(int total, int i) {
		return (width - (total - 1) * CARD_SCALE) / 2 + i * CARD_SCALE;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		var source = Minecraft.getInstance().renderBuffers().bufferSource();

		var itemRenderer = minecraft.getItemRenderer();
		for (var card : enemyHand)
			card.render(mouseX, mouseY, source, itemRenderer);
		for (var card : yourHand)
			card.render(mouseX, mouseY, source, itemRenderer);
		for (var card : enemyBoard)
			card.render(mouseX, mouseY, source, itemRenderer);
		for (var card : yourBoard)
			card.render(mouseX, mouseY, source, itemRenderer);

		source.endBatch();

		super.render(poseStack, mouseX, mouseY, partialTicks);
	}

	private static class ClientCard {

		private Card card;
		private Vec2 position;

		public ClientCard(Card card, Vec2 position) {
			this.card = card;
			this.position = position;
		}

		public void render(int mouseX, int mouseY, BufferSource source, ItemRenderer renderer) {
			var stack = ModItems.CARD.get().getDefaultInstance();
			CardData.get(stack).ifPresent(data -> data.setType(card.getType()));
			var ps = new PoseStack();
			ps.pushPose();
			ps.translate(position.x, position.y, CARD_Z);
			ps.scale(CARD_SCALE, -CARD_SCALE, CARD_SCALE);
			int light = contains(mouseX, mouseY) ? CARD_LIGHT : CARD_LIGHT_HOVER;
			renderer.renderStatic(null, stack, TransformType.GUI, false, ps, source, null, light,
					OverlayTexture.NO_OVERLAY, 0);
			ps.popPose();
		}

		private boolean contains(int x, int y) {
			return x > position.x - CARD_WIDTH / 2 && x < position.x + CARD_WIDTH / 2
					&& y > position.y - CARD_HEIGHT / 2 && y < position.y + CARD_HEIGHT / 2;
		}

	}
}
