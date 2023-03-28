package mod.vemerion.minecard.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.ClientPlayerState;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.network.AttackMessage;
import mod.vemerion.minecard.network.EndTurnMessage;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.PlayCardMessage;
import mod.vemerion.minecard.screen.animation.Animation;
import mod.vemerion.minecard.screen.animation.ThrowItemAnimation;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class GameScreen extends Screen {

	public static final Component TITLE = new TranslatableComponent("gui." + Main.MODID + ".game");

	private static Component YOUR_TURN = new TranslatableComponent(Helper.gui("your_turn"));
	private static Component ENEMY_TURN = new TranslatableComponent(Helper.gui("enemy_turn"));
	private static Component GAME_OVER = new TranslatableComponent(Helper.gui("game_over"));

	public static final int CARD_SCALE = 60;
	public static final int CARD_LIGHT = LightTexture.FULL_BRIGHT;
	public static final int CARD_LIGHT_HOVER = 0b011000000000000001100000;
	public static final int CARD_WIDTH = 46;
	public static final int CARD_HEIGHT = 48;
	private static final int NEXT_TURN_BUTTON_SIZE = 20;
	private static final int DECK_HORIZONTAL_OFFSET = 20;
	private static final int DECK_VERTICAL_OFFSET = 20;

	// State
	Map<UUID, ClientPlayerState> state;
	private UUID current = UUID.randomUUID();
	private BlockPos pos;

	// Widgets
	private PopupText popup;
	private List<Animation> animations;

	private Card selectedCard;
	private Card attackingCard;

	public GameScreen(List<ClientPlayerState> list, BlockPos pos) {
		super(TITLE);
		this.state = initState(list);
		this.pos = pos;
		this.popup = new PopupText();
		updateState();
		this.animations = new ArrayList<>();
	}

	public Card getSelectedCard() {
		return selectedCard;
	}

	public Card getAttackingCard() {
		return attackingCard;
	}

	private Map<UUID, ClientPlayerState> initState(List<ClientPlayerState> list) {
		Map<UUID, ClientPlayerState> map = new HashMap<>();
		for (var playerState : list)
			map.put(playerState.id, playerState);
		return map;
	}

	@Override
	protected void init() {
		super.init();
		for (var playerState : state.values())
			resetPositions(playerState);
		addRenderableWidget(new NextTurnButton((int) (width * 0.75), height / 2 - NEXT_TURN_BUTTON_SIZE / 2,
				NEXT_TURN_BUTTON_SIZE, NEXT_TURN_BUTTON_SIZE, TextComponent.EMPTY));
	}

	public void setCurrent(UUID current) {
		this.current = current;
		popup.popup(current.equals(minecraft.player.getUUID()) ? YOUR_TURN : ENEMY_TURN);
	}

	public void setResources(UUID id, int resources, int maxResources) {
		var playerState = state.get(id);
		playerState.resources = resources;
		playerState.maxResources = maxResources;
	}

	public void placeCard(UUID id, Card card, int cardIndex, int position) {
		var playerState = state.get(id);
		playerState.board.add(position,
				new ClientCard(card, ((ClientCard) playerState.hand.get(cardIndex)).getPosition(), this));
		playerState.hand.remove(cardIndex);
		resetPositions(playerState);
	}

	public void setReady(UUID id, List<Integer> cards) {
		var playerState = state.get(id);
		for (var card : cards)
			playerState.board.get(card).setReady(true);
	}

	public Card updateCard(UUID id, Card card, int position) {
		var playerState = state.get(id);
		if (card.isDead()) {
			card = playerState.board.remove(position);
		} else {
			card = playerState.board.set(position,
					new ClientCard(card, ((ClientCard) playerState.board.get(position)).getPosition(), this));
		}

		resetPositions(playerState);
		return card;
	}

	public void drawCard(UUID id, Card card, boolean shrinkDeck) {
		var playerState = state.get(id);
		boolean enemy = !minecraft.player.getUUID().equals(id);
		float x = enemy ? DECK_HORIZONTAL_OFFSET : width - DECK_HORIZONTAL_OFFSET - CARD_WIDTH;
		float y = enemy ? DECK_VERTICAL_OFFSET : height - DECK_VERTICAL_OFFSET - CARD_HEIGHT;
		playerState.hand.add(new ClientCard(card, new Vec2(x, y), this));
		resetPositions(playerState);

		if (shrinkDeck)
			playerState.deck--;
	}

	public void gameOver() {
		popup.popup(GAME_OVER);
	}

	public void combat(UUID attackerId, Card attackerCard, int attackerPos, UUID targetId, Card targetCard,
			int targetPos) {
		ClientCard attacker = (ClientCard) updateCard(attackerId, attackerCard, attackerPos);
		ClientCard target = (ClientCard) updateCard(targetId, targetCard, targetPos);

		animations.add(new ThrowItemAnimation(minecraft, new ItemStack(Items.STONE_SWORD),
				new Vec2(attacker.getPosition().x + CARD_WIDTH / 2, attacker.getPosition().y + CARD_HEIGHT / 2),
				target));
	}

	private ClientPlayerState yourState() {
		return state.get(minecraft.player.getUUID());
	}

	private ClientPlayerState enemyState() {
		for (var playerState : state.values())
			if (!playerState.id.equals(minecraft.player.getUUID()))
				return playerState;
		return null;
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		if (isCurrentActive()) {
			if (pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

				// Play card
				if (selectedCard == null) {
					for (var card : yourState().hand) {
						if (((ClientCard) card).contains(pMouseX, pMouseY)) {
							selectedCard = card;
							return true;
						}
					}
				} else {
					if (pMouseY + CARD_HEIGHT / 2 < height - CARD_HEIGHT && pMouseY > height - CARD_HEIGHT * 2) {
						int order = 0;
						int cardIndex = 0;
						for (int i = 0; i < yourState().board.size(); i++) {
							var card = (ClientCard) yourState().board.get(i);
							if (pMouseX < ((ClientCard) card).getPosition().x + CARD_WIDTH / 2) {
								order = i;
								break;
							} else {
								order = i + 1;
							}
						}
						for (int i = 0; i < yourState().hand.size(); i++) {
							if (yourState().hand.get(i) == selectedCard) {
								cardIndex = i;
								break;
							}
						}
						Network.INSTANCE.sendToServer(new PlayCardMessage(pos, cardIndex, order));
						selectedCard = null;
						return true;
					}
				}

				// Attack
				if (selectedCard == null && attackingCard == null) {
					for (var card : yourState().board) {
						if (((ClientCard) card).contains(pMouseX, pMouseY) && card.isReady()) {
							attackingCard = card;
							return true;
						}
					}
				} else if (attackingCard != null) {
					var enemy = enemyState().board;
					for (int i = 0; i < enemy.size(); i++) {
						if (((ClientCard) enemy.get(i)).contains(pMouseX, pMouseY)) {
							for (int j = 0; j < yourState().board.size(); j++) {
								if (yourState().board.get(j) == attackingCard) {
									Network.INSTANCE.sendToServer(new AttackMessage(pos, j, i));
									break;
								}
							}
							attackingCard = null;
							return true;
						}
					}
				}
			} else if (pButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
				selectedCard = null;
				attackingCard = null;
			}
		}
		return super.mouseClicked(pMouseX, pMouseY, pButton);
	}

	private void updateState() {
		for (var playerState : state.values()) {
			for (int i = 0; i < playerState.hand.size(); i++) {
				playerState.hand.set(i, new ClientCard(playerState.hand.get(i), Vec2.ZERO, this));
			}
			for (int i = 0; i < playerState.board.size(); i++) {
				playerState.board.set(i, new ClientCard(playerState.board.get(i), Vec2.ZERO, this));
			}
		}
	}

	private void resetPositions(ClientPlayerState playerState) {
		boolean enemy = !playerState.id.equals(minecraft.player.getUUID());
		for (int i = 0; i < playerState.hand.size(); i++) {
			int x = cardRowX(playerState.hand.size(), enemy ? playerState.hand.size() - i - 1 : i);
			int y = enemy ? 5 : height - CARD_HEIGHT - 5;
			((ClientCard) playerState.hand.get(i)).setPosition(new Vec2(x, y));
		}
		for (int i = 0; i < playerState.board.size(); i++) {
			int x = cardRowX(playerState.board.size(), enemy ? playerState.board.size() - i - 1 : i);
			int y = enemy ? CARD_HEIGHT + 20 : height - CARD_HEIGHT * 2 - 20;
			((ClientCard) playerState.board.get(i)).setPosition(new Vec2(x, y));
		}
	}

	private int cardRowX(int total, int i) {
		return (width - (total - 1) * CARD_WIDTH) / 2 + i * CARD_WIDTH - CARD_WIDTH / 2;
	}

	private boolean isCurrentActive() {
		return current.equals(minecraft.player.getUUID());
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		partialTicks = this.minecraft.getFrameTime(); // s
		var source = minecraft.renderBuffers().bufferSource();

		for (var playerState : state.values()) {
			boolean enemy = !playerState.id.equals(minecraft.player.getUUID());

			// Cards
			for (var card : playerState.board)
				((ClientCard) card).render(mouseX, mouseY, source, partialTicks);
			for (var card : playerState.hand)
				((ClientCard) card).render(mouseX, mouseY, source, partialTicks);

			// Deck
			for (int i = 0; i < playerState.deck; i++) {
				float x = enemy ? DECK_HORIZONTAL_OFFSET + i * 0.2f
						: width - DECK_HORIZONTAL_OFFSET - CARD_WIDTH + i * 0.2f;
				float y = enemy ? DECK_VERTICAL_OFFSET : height - DECK_VERTICAL_OFFSET - CARD_HEIGHT;
				new ClientCard(Cards.EMPTY, new Vec2(x, y), this).render(mouseX, mouseY, source, partialTicks);
			}

			// Resources
			int resourcesX = enemy ? 200 : width - 200;
			int resourcesY = enemy ? 60 : height - 60;
			renderResources(playerState.maxResources, new Vec3(resourcesX, resourcesY, 0), enemy, 0, poseStack, source);
			renderResources(playerState.resources, new Vec3(resourcesX, resourcesY, 0.1), enemy,
					LightTexture.FULL_BRIGHT, poseStack, source);
		}

		for (var animation : animations)
			animation.render(mouseX, mouseY, source, partialTicks);

		source.endBatch();

		popup.render(poseStack, mouseX, mouseY, partialTicks);

		super.render(poseStack, mouseX, mouseY, partialTicks);
	}

	private void renderResources(int count, Vec3 position, boolean reverse, int light, PoseStack poseStack,
			BufferSource source) {
		var stack = Items.EMERALD.getDefaultInstance();
		for (int i = 0; i < count; i++) {
			poseStack.pushPose();
			poseStack.translate(position.x + i * 10 * (reverse ? -1 : 1), position.y, position.z);
			poseStack.scale(15, -15, 15);
			itemRenderer.renderStatic(stack, ItemTransforms.TransformType.GUI, light, OverlayTexture.NO_OVERLAY,
					poseStack, source, 0);
			poseStack.popPose();
		}
	}

	@Override
	public void tick() {
		super.tick();

		for (var playerState : state.values()) {
			// Cards
			for (var card : playerState.board)
				((ClientCard) card).tick();
			for (var card : playerState.hand)
				((ClientCard) card).tick();
		}

		for (var animation : animations)
			animation.tick();

		for (int i = animations.size() - 1; i >= 0; i--)
			if (animations.get(i).isDone())
				animations.remove(i);

		popup.tick();
	}

	private class NextTurnButton extends AbstractButton {

		private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/next_turn.png");
		private static final TranslatableComponent NEXT_TURN = new TranslatableComponent(Helper.gui("next_turn"));

		public NextTurnButton(int pX, int pY, int pWidth, int pHeight, Component pMessage) {
			super(pX, pY, pWidth, pHeight, pMessage);
		}

		@Override
		public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

		}

		@Override
		public void onPress() {
			if (isCurrentActive()) {
				Network.INSTANCE.sendToServer(new EndTurnMessage(pos));
				selectedCard = null;
				attackingCard = null;
			}
		}

		@Override
		public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, TEXTURE);

			RenderSystem.enableDepthTest();
			RenderSystem.setShaderColor(isHovered ? 0.6f : 1, isHovered ? 0.6f : 1, 1, 1);
			blit(pPoseStack, x, y, isCurrentActive() ? 0 : NEXT_TURN_BUTTON_SIZE, 0, width, height,
					NEXT_TURN_BUTTON_SIZE * 2, NEXT_TURN_BUTTON_SIZE);
			if (isHovered && isCurrentActive()) {
				GameScreen.this.renderTooltip(pPoseStack, NEXT_TURN, pMouseX, pMouseY);
			}
		}

	}

	private class PopupText implements Widget {
		private Component text;
		private int alpha;
		private float scale;

		private PopupText() {
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

		private void popup(Component text) {
			this.text = text;
			alpha = 255;
			scale = 3;
		}

	}
}
