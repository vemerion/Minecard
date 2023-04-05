package mod.vemerion.minecard.screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.MessagePlayerState;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.network.AttackMessage;
import mod.vemerion.minecard.network.EndTurnMessage;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.PlayCardMessage;
import mod.vemerion.minecard.screen.animation.Animation;
import mod.vemerion.minecard.screen.animation.DeathAnimation;
import mod.vemerion.minecard.screen.animation.TauntAnimation;
import mod.vemerion.minecard.screen.animation.ThrowItemAnimation;
import mod.vemerion.minecard.screen.animation.WallAnimation;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;

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

	private static final Card EMPTY_CARD = Cards.EMPTY_CARD_TYPE.create();

	// State
	Map<UUID, ClientPlayerState> state;
	private UUID current = UUID.randomUUID();
	private BlockPos pos;

	// Widgets
	private PopupText popup;
	private List<Animation> animations;
	private List<Resources> resources;

	private Card selectedCard;
	private Card attackingCard;

	private float fovModifier = 1;

	public GameScreen(List<MessagePlayerState> list, BlockPos pos) {
		super(TITLE);
		this.animations = new ArrayList<>();
		this.state = initState(list);
		this.pos = pos;
		this.popup = new PopupText();
	}

	public Card getSelectedCard() {
		return selectedCard;
	}

	public Card getAttackingCard() {
		return attackingCard;
	}

	private Map<UUID, ClientPlayerState> initState(List<MessagePlayerState> list) {
		Map<UUID, ClientPlayerState> map = new HashMap<>();
		for (var messageState : list)
			map.put(messageState.id,
					new ClientPlayerState(messageState.id, messageState.deck,
							messageState.hand.stream().map(c -> new ClientCard(c, Vec2.ZERO, this))
									.collect(Collectors.toList()),
							messageState.board.stream().map(c -> new ClientCard(c, Vec2.ZERO, this))
									.collect(Collectors.toList()),
							messageState.resources, messageState.maxResources));
		return map;
	}

	@Override
	protected void init() {
		super.init();
		resources = new ArrayList<>();
		for (var playerState : state.values()) {
			resetPositions(playerState);

			boolean enemy = !playerState.id.equals(minecraft.player.getUUID());
			resources.add(new Resources(playerState.id, enemy));
		}
		addRenderableWidget(new NextTurnButton((int) (width * 0.75), height / 2 - NEXT_TURN_BUTTON_SIZE / 2,
				NEXT_TURN_BUTTON_SIZE, NEXT_TURN_BUTTON_SIZE, TextComponent.EMPTY));

		animations = new ArrayList<>();
		for (var playerState : state.values())
			for (var card : playerState.board)
				updatePropertiesAnimations(null, card);
	}

	private ClientCard withId(List<ClientCard> list, int id) {
		return list.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
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

	public void placeCard(UUID id, Card card, int position) {
		var playerState = state.get(id);
		var placed = new ClientCard(card, withId(playerState.hand, card.getId()).getPosition(), this);
		playerState.board.add(position, placed);
		playerState.hand.removeIf(c -> c.getId() == card.getId());
		resetPositions(playerState);

		updatePropertiesAnimations(null, placed);
	}

	public void setReady(UUID id, List<Integer> cards) {
		var playerState = state.get(id);
		for (var card : playerState.board)
			if (cards.contains(card.getId()))
				card.setReady(true);
	}

	public ClientCard updateCard(UUID id, Card received) {
		var playerState = state.get(id);

		for (var card : playerState.board) {
			if (card.getId() == received.getId()) {
				card.copy(received);

				if (card.isDead()) {
					animations.add(new DeathAnimation(minecraft, card, 40, () -> {
						playerState.board.removeIf(c -> card.getId() == c.getId());
						card.remove();
						resetPositions(state.get(id));
					}));
				}
				return card;
			}
		}
		return null;
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

	public void combat(UUID attackerId, Card attackerCard, UUID targetId, Card targetCard) {
		var attacker = updateCard(attackerId, attackerCard);
		var target = updateCard(targetId, targetCard);

		animations.add(new ThrowItemAnimation(minecraft, new ItemStack(Items.STONE_SWORD),
				new Vec2(attacker.getPosition().x + CARD_WIDTH / 2, attacker.getPosition().y + CARD_HEIGHT / 2), target,
				() -> {
					minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_ATTACK_SWEEP, 1));
				}));
	}

	public void setProperties(UUID id, int cardId, Map<CardProperty, Integer> properties) {
		var card = withId(state.get(id).board, cardId);
		var old = new HashMap<>(card.getProperties());
		card.getProperties().clear();
		card.getProperties().putAll(properties);
		updatePropertiesAnimations(old, card);
	}

	private void updatePropertiesAnimations(Map<CardProperty, Integer> old, ClientCard card) {
		for (var entry : card.getProperties().entrySet()) {
			if ((old == null || old.getOrDefault(entry.getKey(), 0) < 1) && entry.getValue() > 0) {
				switch (entry.getKey()) {
				case CHARGE:
					fovModifier = 3;
					break;
				case FREEZE:
					break;
				case SHIELD:
					animations.add(new WallAnimation(minecraft, card, () -> {
					}));
					break;
				case STEALTH:
					break;
				case TAUNT:
					animations.add(new TauntAnimation(minecraft, card, () -> {
					}));
					break;
				}
			}
		}
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
						if (card.contains(pMouseX, pMouseY)) {
							selectedCard = card;
							return true;
						}
					}
				} else {
					if (pMouseY + CARD_HEIGHT / 2 < height - CARD_HEIGHT && pMouseY > height - CARD_HEIGHT * 2) {
						int order = 0;
						for (int i = 0; i < yourState().board.size(); i++) {
							var card = yourState().board.get(i);
							if (pMouseX < card.getPosition().x + CARD_WIDTH / 2) {
								order = i;
								break;
							} else {
								order = i + 1;
							}
						}
						Network.INSTANCE.sendToServer(new PlayCardMessage(pos, selectedCard.getId(), order));
						selectedCard = null;
						return true;
					}
				}

				// Attack
				if (selectedCard == null && attackingCard == null) {
					for (var card : yourState().board) {
						if (card.contains(pMouseX, pMouseY) && card.isReady()) {
							attackingCard = card;
							return true;
						}
					}
				} else if (attackingCard != null) {
					for (var card : enemyState().board) {
						if (card.contains(pMouseX, pMouseY)) {
							Network.INSTANCE.sendToServer(new AttackMessage(pos, attackingCard.getId(), card.getId()));
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

	private void resetPositions(ClientPlayerState playerState) {
		boolean enemy = !playerState.id.equals(minecraft.player.getUUID());
		for (int i = 0; i < playerState.hand.size(); i++) {
			int x = cardRowX(playerState.hand.size(), enemy ? playerState.hand.size() - i - 1 : i);
			int y = enemy ? 5 : height - CARD_HEIGHT - 5;
			playerState.hand.get(i).setPosition(new Vec2(x, y));
		}
		for (int i = 0; i < playerState.board.size(); i++) {
			int x = cardRowX(playerState.board.size(), enemy ? playerState.board.size() - i - 1 : i);
			int y = enemy ? CARD_HEIGHT + 20 : height - CARD_HEIGHT * 2 - 20;
			playerState.board.get(i).setPosition(new Vec2(x, y));
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
				if (card.getType() == null || !card.isDead())
					card.render(new PoseStack(), mouseX, mouseY, source, partialTicks);
			for (var card : playerState.hand)
				if (card.getType() == null || !card.isDead())
					card.render(new PoseStack(), mouseX, mouseY, source, partialTicks);

			// Deck
			float deckX = enemy ? DECK_HORIZONTAL_OFFSET : width - DECK_HORIZONTAL_OFFSET - CARD_WIDTH;
			float deckY = enemy ? DECK_VERTICAL_OFFSET : height - DECK_VERTICAL_OFFSET - CARD_HEIGHT;
			for (int i = 0; i < playerState.deck; i++) {
				float x = deckX + i * 0.2f;
				new ClientCard(EMPTY_CARD, new Vec2(x, deckY), this).render(new PoseStack(), mouseX, mouseY, source,
						partialTicks);
			}
			if (mouseX > deckX && mouseX < deckX + CARD_WIDTH && mouseY > deckY && mouseY < deckY + CARD_HEIGHT)
				renderTooltip(poseStack, new TranslatableComponent(Helper.gui("deck_count"), playerState.deck), mouseX,
						mouseY);
		}

		for (var r : resources)
			r.render(new PoseStack(), mouseX, mouseY, partialTicks, source);

		for (var animation : animations)
			animation.render(mouseX, mouseY, source, partialTicks);

		source.endBatch();

		popup.render(poseStack, mouseX, mouseY, partialTicks);

		super.render(poseStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void tick() {
		super.tick();

		for (var playerState : state.values()) {
			// Cards
			for (var card : playerState.board)
				card.tick();
			for (var card : playerState.hand)
				card.tick();
		}

		for (var r : resources)
			r.tick();

		for (var animation : animations)
			animation.tick();

		for (int i = animations.size() - 1; i >= 0; i--)
			if (animations.get(i).isDone()) {
				animations.get(i).onDone();
				animations.remove(i);
			}

		popup.tick();

		fovModifier = (float) Mth.lerp(0.08, fovModifier, 1);
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

	private class Resources {

		private static final float SCALE = 15;
		private static final int OFFSET = 10;

		private UUID id;
		private boolean top;

		private float[] scales = new float[20];
		private float[] scales0 = new float[20];

		private Resources(UUID id, boolean top) {
			this.id = id;
			this.top = top;
		}

		public void render(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick, BufferSource source) {
			var position = new Vec2((top ? 200 : width - 200), top ? 60 : height - 60);
			for (int i = 0; i < 10; i++) {
				poseStack.pushPose();
				poseStack.translate(position.x + i * OFFSET * (top ? -1 : 1), position.y, 0);

				renderResource(poseStack, pPartialTick, source, i * 2, true);
				renderResource(poseStack, pPartialTick, source, i * 2 + 1, false);

				poseStack.popPose();
			}

			// Render count text
			var playerState = state.get(id);
			int width = Math.max(playerState.resources, playerState.maxResources) * OFFSET;
			position = position.add(new Vec2(OFFSET / 2 * (top ? 1 : -1), -OFFSET / 2));
			if (pMouseX > (top ? position.x - width : position.x) && pMouseX < (top ? position.x : position.x + width)
					&& pMouseY > position.y && pMouseY < position.y + OFFSET)
				GameScreen.this.renderTooltip(poseStack, new TranslatableComponent(Helper.gui("resources_count"),
						playerState.resources, playerState.maxResources), pMouseX, pMouseY);
		}

		private void renderResource(PoseStack poseStack, float pPartialTick, BufferSource source, int index,
				boolean background) {
			poseStack.pushPose();
			float scale = SCALE * Mth.lerp(pPartialTick, scales0[index], scales[index]);
			poseStack.scale(scale, -scale, scale);
			if (!background)
				poseStack.translate(0, 0, 0.1);
			itemRenderer.renderStatic(Items.EMERALD.getDefaultInstance(), ItemTransforms.TransformType.GUI,
					background ? 0 : LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, source, 0);
			poseStack.popPose();
		}

		private void tick() {
			var playerState = state.get(id);
			for (int i = 0; i < 10; i++) {
				updateResource(i * 2, i < playerState.maxResources);
				updateResource(i * 2 + 1, i < playerState.resources);
			}
		}

		private void updateResource(int index, boolean increase) {
			scales0[index] = scales[index];
			if (increase)
				scales[index] = (float) Mth.lerp(0.1, scales[index], 1);
			else
				scales[index] = (float) Mth.lerp(0.1, scales[index], 0);
		}

	}

	public float getFovModifier() {
		return fovModifier;
	}
}
