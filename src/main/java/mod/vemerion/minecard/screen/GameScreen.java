package mod.vemerion.minecard.screen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.MessagePlayerState;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.network.AttackMessage;
import mod.vemerion.minecard.network.CloseGameMessage;
import mod.vemerion.minecard.network.EndTurnMessage;
import mod.vemerion.minecard.network.GameClient;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.PlayCardMessage;
import mod.vemerion.minecard.network.PlayerChoiceResponseMessage;
import mod.vemerion.minecard.renderer.CardItemRenderer;
import mod.vemerion.minecard.screen.animation.Animation;
import mod.vemerion.minecard.screen.animation.AttackAnimation;
import mod.vemerion.minecard.screen.animation.BurnAnimation;
import mod.vemerion.minecard.screen.animation.DeathAnimation;
import mod.vemerion.minecard.screen.animation.FreezeAnimation;
import mod.vemerion.minecard.screen.animation.PoisonAnimation;
import mod.vemerion.minecard.screen.animation.StealthAnimation;
import mod.vemerion.minecard.screen.animation.TauntAnimation;
import mod.vemerion.minecard.screen.animation.ThornsAnimation;
import mod.vemerion.minecard.screen.animation.WallAnimation;
import mod.vemerion.minecard.screen.animation.config.AnimationConfigs;
import net.minecraft.client.Minecraft;
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
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class GameScreen extends Screen implements GameClient {

	public static final Component TITLE = new TranslatableComponent("gui." + Main.MODID + ".game");

	private static Component NEXT_TURN = new TranslatableComponent(Helper.gui("next_turn"));
	private static Component GAME_OVER = new TranslatableComponent(Helper.gui("game_over"));
	private static Component CHOOSE_TEXT = new TranslatableComponent(Helper.gui("choose"));

	public static final int CARD_SCALE = 60;
	public static final int CARD_LIGHT = LightTexture.FULL_BRIGHT;
	public static final int CARD_LIGHT_HOVER = 0b011000000000000001100000;
	public static final int CARD_WIDTH = 42;
	public static final int CARD_HEIGHT = 48;
	private static final int NEXT_TURN_BUTTON_SIZE = 20;
	private static final int DECK_HORIZONTAL_OFFSET = 20;
	private static final int DECK_VERTICAL_OFFSET = 20;
	private static final int CARD_PADDING = 4;
	private static final int CARD_WITH_PADDING = CARD_WIDTH + CARD_PADDING;

	private static final Card EMPTY_CARD = Cards.EMPTY_CARD_TYPE.create();

	// State
	Map<UUID, ClientPlayerState> state;
	private UUID current = UUID.randomUUID();
	private BlockPos pos;
	private boolean isSpectator = true;
	private Choices choices = new Choices();

	// Widgets
	private PopupText popup;
	private List<Animation> animations;
	private List<Resources> resources;
	private GameBackground background;

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

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Override
	public void openGame(List<MessagePlayerState> state, BlockPos pos) {
	}

	public Card getSelectedCard() {
		return selectedCard;
	}

	public Card getAttackingCard() {
		return attackingCard;
	}

	public void addAnimation(Animation anim) {
		animations.add(anim);
	}

	private Map<UUID, ClientPlayerState> initState(List<MessagePlayerState> list) {
		isSpectator = list.stream().noneMatch(s -> s.id.equals(Minecraft.getInstance().player.getUUID()));

		Map<UUID, ClientPlayerState> map = new HashMap<>();
		boolean isTop = false;
		for (var messageState : list) {
			map.put(messageState.id,
					new ClientPlayerState(messageState.id, messageState.deck,
							messageState.hand.stream().map(c -> new ClientCard(c, Vec2.ZERO, this))
									.collect(Collectors.toList()),
							messageState.board.stream().map(c -> new ClientCard(c, Vec2.ZERO, this))
									.collect(Collectors.toList()),
							messageState.resources, messageState.maxResources,
							isSpectator ? isTop : !messageState.id.equals(Minecraft.getInstance().player.getUUID())));
			isTop = !isTop;
		}
		return map;
	}

	@Override
	protected void init() {
		super.init();
		resources = new ArrayList<>();

		for (var playerState : state.values()) {
			resetPositions(playerState);

			resources.add(new Resources(playerState.id, playerState.isTop));
		}

		if (!isSpectator)
			addRenderableWidget(new NextTurnButton((int) (width * 0.75), height / 2 - NEXT_TURN_BUTTON_SIZE / 2,
					NEXT_TURN_BUTTON_SIZE, NEXT_TURN_BUTTON_SIZE, TextComponent.EMPTY));

		background = addWidget(new GameBackground(this));

		animations = new ArrayList<>();
		for (var playerState : state.values())
			for (var card : playerState.board)
				updatePropertiesAnimations(null, card);

		choices.update();
	}

	@Override
	public void onClose() {
		Network.INSTANCE.sendToServer(new CloseGameMessage(pos));
		super.onClose();
	}

	private ClientCard withId(int id) {
		for (var playerState : state.values()) {
			for (var card : playerState.board)
				if (card.getId() == id)
					return card;
			for (var card : playerState.hand)
				if (card.getId() == id)
					return card;
		}
		return null;
	}

	private ClientCard withId(List<ClientCard> list, int id) {
		return list.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
	}

	@Override
	public void setCurrent(UUID current) {
		this.current = current;
		popup.popup(NEXT_TURN);
	}

	@Override
	public void setResources(UUID id, int resources, int maxResources) {
		var playerState = state.get(id);
		playerState.resources = resources;
		playerState.maxResources = maxResources;
	}

	@Override
	public void placeCard(UUID id, Card card, int leftId) {
		var playerState = state.get(id);

		if (!card.isSpell()) {
			var fromHand = withId(playerState.hand, card.getId());
			var pos = fromHand == null ? new Vec2((width - CARD_WIDTH) / 2, (height - CARD_HEIGHT) / 2)
					: fromHand.getPosition();
			var placed = new ClientCard(card, pos, this);
			if (fromHand == null)
				placed.appear(1);
			if (leftId == -1) {
				playerState.board.add(0, placed);
			} else {
				for (int i = 0; i < playerState.board.size(); i++) {
					if (playerState.board.get(i).getId() == leftId) {
						playerState.board.add(i + 1, placed);
						break;
					}
				}
			}
			updatePropertiesAnimations(null, placed);
		}
		playerState.hand.removeIf(c -> c.getId() == card.getId());
		resetPositions(playerState);
	}

	@Override
	public void setReady(UUID id, List<Integer> cards) {
		var playerState = state.get(id);
		for (var card : playerState.board)
			if (cards.contains(card.getId()))
				card.setReady(true);
	}

	@Override
	public void updateCard(Card received) {
		for (var playerState : state.values()) {
			for (var card : playerState.board) {
				if (card.getId() == received.getId()) {
					var old = new HashMap<>(card.getProperties());
					card.copy(received);

					if (card.isDead()) {
						animations.add(new DeathAnimation(minecraft, card, 20, () -> {
							playerState.board.removeIf(c -> card.getId() == c.getId());
							card.remove();
							resetPositions(playerState);
						}));
					}

					updatePropertiesAnimations(old, card);

					return;
				}
			}
			for (var card : playerState.hand) {
				if (card.getId() == received.getId()) {
					card.copy(received);

					if (card.isDead()) {
						animations.add(new DeathAnimation(minecraft, card, 0, () -> {
							playerState.hand.removeIf(c -> card.getId() == c.getId());
							card.remove();
							resetPositions(playerState);
						}));
					}

					return;
				}
			}
		}
	}

	@Override
	public void drawCards(UUID id, List<Card> cards, boolean shrinkDeck) {
		var playerState = state.get(id);
		boolean enemy = playerState.isTop;
		float x = enemy ? DECK_HORIZONTAL_OFFSET : width - DECK_HORIZONTAL_OFFSET - CARD_WIDTH;
		float y = enemy ? DECK_VERTICAL_OFFSET : height - DECK_VERTICAL_OFFSET - CARD_HEIGHT;

		List<ClientCard> added = new ArrayList<>();
		for (var card : cards) {
			var c = new ClientCard(card, new Vec2(x, y), this);
			added.add(c);
			playerState.hand.add(c);
		}

		resetPositions(playerState);

		if (shrinkDeck) {
			playerState.deck -= cards.size();
		} else {
			for (var card : added) {
				card.resetPosition();
				card.appear(1);
			}
		}
	}

	@Override
	public void gameOver() {
		popup.popup(GAME_OVER);
	}

	@Override
	public void combat(UUID attackerId, int attackerCardId, UUID targetId, int targetCardId) {
		var attacker = withId(state.get(attackerId).board, attackerCardId);
		var target = withId(state.get(targetId).board, targetCardId);

		animations.add(new AttackAnimation(minecraft, attacker, target));
	}

	@Override
	public void setProperties(UUID id, int cardId, Map<CardProperty, Integer> properties) {
		var card = withId(state.get(id).board, cardId);
		var old = new HashMap<>(card.getProperties());
		card.getProperties().clear();
		card.getProperties().putAll(properties);
		updatePropertiesAnimations(old, card);
	}

	@Override
	public void playerChoice(Choice choice) {
		choices.add(choice);
	}

	private void updatePropertiesAnimations(Map<CardProperty, Integer> old, ClientCard card) {
		for (var entry : card.getProperties().entrySet()) {
			if ((old == null || old.getOrDefault(entry.getKey(), 0) < 1) && entry.getValue() > 0) {
				switch (entry.getKey()) {
				case CHARGE:
					fovModifier = 3;
					break;
				case FREEZE:
					animations.add(new FreezeAnimation(minecraft, card, () -> {
					}));
					break;
				case SHIELD:
					animations.add(new WallAnimation(minecraft, card, () -> {
					}));
					break;
				case STEALTH:
					animations.add(new StealthAnimation(minecraft, card, () -> {
					}));
					break;
				case TAUNT:
					animations.add(new TauntAnimation(minecraft, card, () -> {
					}));
					break;
				case BURN:
					animations.add(new BurnAnimation(minecraft, card, () -> {
					}));
					break;
				case THORNS:
					animations.add(new ThornsAnimation(minecraft, card, () -> {
					}));
					break;
				case SPECIAL:
					break;
				case BABY:
					break;
				case POISON:
					animations.add(new PoisonAnimation(minecraft, card, () -> {
					}));
					break;
				}
			}
		}
	}

	@Override
	public void updateDecks(Map<UUID, Integer> sizes) {
		for (var entry : sizes.entrySet())
			state.get(entry.getKey()).deck = entry.getValue();
	}

	@Override
	public void animation(int originId, List<Integer> targets, ResourceLocation rl) {
		var animConfig = AnimationConfigs.getInstance().get(rl);
		if (animConfig == null)
			return;

		animConfig.invoke(this, withId(originId), targets.stream().map(id -> withId(id)).filter(c -> c != null)
				.collect(Collectors.toCollection(() -> new ArrayList<>())));
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
		if (isCurrentActive() && !isSpectator) {
			if (pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {

				// Player choice
				if (choices.mouseClicked(pMouseX, pMouseY)) {
					return true;
				} else {
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
							int leftId = -1;
							for (var card : yourState().board) {
								if (card.isDead())
									continue;

								if (pMouseX < card.getPosition().x + CARD_WIDTH / 2) {
									break;
								} else {
									leftId = card.getId();
								}
							}
							Network.INSTANCE.sendToServer(new PlayCardMessage(pos, selectedCard.getId(), leftId));
							selectedCard = null;
							return true;
						}
					}

					// Attack
					if (selectedCard == null && attackingCard == null) {
						for (var card : yourState().board) {
							if (card.contains(pMouseX, pMouseY) && card.canAttack()) {
								attackingCard = card;
								playAmbientSound(attackingCard);
								return true;
							}
						}
					} else if (attackingCard != null) {
						for (var card : enemyState().board) {
							if (card.contains(pMouseX, pMouseY)) {
								Network.INSTANCE
										.sendToServer(new AttackMessage(pos, attackingCard.getId(), card.getId()));
								attackingCard = null;
								return true;
							}
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
		boolean enemy = playerState.isTop;
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
		return (width - (total - 1) * CARD_WITH_PADDING) / 2 + i * CARD_WITH_PADDING - CARD_WIDTH / 2;
	}

	private boolean isCurrentActive() {
		return current.equals(minecraft.player.getUUID());
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
		Lighting.setupForFlatItems();
		partialTicks = this.minecraft.getFrameTime(); // s
		var source = minecraft.renderBuffers().bufferSource();

		background.render(poseStack, mouseX, mouseY, source, partialTicks);

		for (var playerState : state.values()) {
			boolean enemy = playerState.isTop;

			// Cards
			for (var card : playerState.board) {
				if (card.getType() == null || !card.isDead())
					card.render(new PoseStack(), mouseX, mouseY, source, partialTicks);
			}
			for (var card : playerState.hand)
				if (card.getType() == null || !card.isDead() || card.isSpell())
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

		// Indicate which cards can't be attacked
		if (attackingCard != null) {
			var enemyState = enemyState();
			for (var card : enemyState.board)
				if (!GameUtil.canBeAttacked(card, enemyState.board))
					drawBarrier(poseStack, source, card);
		}

		for (var r : resources)
			r.render(new PoseStack(), mouseX, mouseY, partialTicks, source);

		for (var animation : animations)
			animation.render(mouseX, mouseY, source, partialTicks);

		choices.render(poseStack, mouseX, mouseY, source, partialTicks);

		source.endBatch();

		popup.render(poseStack, mouseX, mouseY, partialTicks);

		super.render(poseStack, mouseX, mouseY, partialTicks);
	}

	private void drawBarrier(PoseStack poseStack, BufferSource source, ClientCard card) {
		poseStack.pushPose();
		poseStack.translate(card.getPosition().x + CARD_WIDTH / 2, card.getPosition().y + CARD_HEIGHT / 2, 10);
		poseStack.scale(30, -30, 30);
		itemRenderer.renderStatic(Items.BARRIER.getDefaultInstance(), ItemTransforms.TransformType.GUI,
				LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, source, 0);
		poseStack.popPose();
	}

	@Override
	public void tick() {
		super.tick();

		CardItemRenderer.getRobot(minecraft.level).guiTick();

		background.tick();

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

		choices.tick();

		fovModifier = (float) Mth.lerp(0.08, fovModifier, 1);
	}

	private static final Method GET_AMBIENT_SOUND = ObfuscationReflectionHelper.findMethod(Mob.class, "m_7515_");

	private void playAmbientSound(Card card) {
		if (CardItemRenderer.getEntity(card, minecraft.level) instanceof Mob entity) {
			try {
				var sound = GET_AMBIENT_SOUND.invoke(entity) instanceof SoundEvent deathSound ? deathSound
						: SoundEvents.ITEM_FRAME_ADD_ITEM;
				minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				Main.LOGGER.error("Unable to play ambient sound for card " + card.getType().getRegistryName().toString()
						+ ": " + e);
			}
		}
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
			if (isCurrentActive() && choices.isEmpty()) {
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
			this.text = NEXT_TURN;
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

	private class Choices {
		private static final int CARD_DISTANCE = 100;
		private List<Choice> choices = new ArrayList<>();
		private List<ClientCard> cards = new ArrayList<>();

		private void add(Choice choice) {
			choices.add(choice);
			if (choices.size() == 1) {
				update();
			}
		}

		private void pop() {
			choices.remove(0);
			update();
		}

		private void update() {
			cards = new ArrayList<>();
			if (!isEmpty() && !choices.get(0).targeting()) {
				var selectable = choices.get(0).cards();
				var startX = (width - CARD_WIDTH) / 2 - (selectable.size() - 1) * CARD_DISTANCE / 2;
				for (int i = 0; i < selectable.size(); i++) {
					var card = new ClientCard(selectable.get(i),
							new Vec2(startX + i * CARD_DISTANCE, (height - CARD_HEIGHT) / 2), GameScreen.this);
					card.appear(2);
					cards.add(card);
				}
			}
		}

		private boolean isEmpty() {
			return choices.isEmpty();
		}

		private void tick() {
			for (var card : cards)
				card.tick();
		}

		private void render(PoseStack poseStack, int mouseX, int mouseY, BufferSource source, float partialTick) {
			poseStack.pushPose();
			poseStack.translate(0, 0, 15);
			for (var card : cards)
				card.render(poseStack, mouseX, mouseY, source, partialTick);
			poseStack.popPose();

			if (!isEmpty()) {

				// Indicate which cards can't be chosen
				var choice = choices.get(0);
				var candidates = choice.cards();
				if (choice.targeting()) {
					for (var playerState : state.values()) {
						for (var list : List.of(playerState.board, playerState.hand)) {
							for (var card : list) {
								if (!candidates.stream().anyMatch(c -> c.getId() == card.getId())) {
									drawBarrier(poseStack, source, card);
								}
							}
						}
					}
				}

				// Text
				poseStack.pushPose();
				poseStack.scale(1, 1, 1);
				poseStack.translate(0, 0, 20);

				poseStack.pushPose();
				poseStack.translate((width - font.width(CHOOSE_TEXT) * 1.5f) / 2, 2, 0);
				poseStack.scale(1.5f, 1.5f, 1.5f);
				font.drawShadow(poseStack, CHOOSE_TEXT, 0, 0, 0xffffff);
				poseStack.popPose();

				var lines = font.split(choices.get(0).ability().getDescription(), 200);
				float y = 18;
				for (var line : lines) {
					font.drawShadow(poseStack, line, (width - font.width(line)) / 2, y, 0xffffff);
					y += 9.5;
				}
				poseStack.popPose();
			}
		}

		private boolean mouseClicked(double pMouseX, double pMouseY) {
			if (choices.isEmpty())
				return false;

			selectedCard = null;
			attackingCard = null;

			var choice = choices.get(0);
			if (choice.targeting()) { // Select target on board/in hand
				for (var playerState : state.values()) {
					for (var list : List.of(playerState.board, playerState.hand)) {
						for (var card : list) {
							if (card.contains(pMouseX, pMouseY)
									&& choice.cards().stream().anyMatch(c -> c.getId() == card.getId())) {
								Network.INSTANCE
										.sendToServer(new PlayerChoiceResponseMessage(pos, choice.id(), card.getId()));
								pop();
								return true;
							}
						}
					}
				}
			} else { // Select choice from sent cards
				for (var card : cards) {
					if (card.contains(pMouseX, pMouseY)) {
						Network.INSTANCE.sendToServer(new PlayerChoiceResponseMessage(pos, choice.id(), card.getId()));
						pop();
						return true;
					}
				}
			}
			return false;
		}
	}

	public float getFovModifier() {
		return fovModifier;
	}
}
