package mod.vemerion.minecard.screen;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.PlayerStats;
import mod.vemerion.minecard.capability.PlayerStats.StatLine;
import mod.vemerion.minecard.game.AIPlayer;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardProperties;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.MessagePlayerState;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.init.ModEntities;
import mod.vemerion.minecard.network.AttackMessage;
import mod.vemerion.minecard.network.CloseGameMessage;
import mod.vemerion.minecard.network.EndTurnMessage;
import mod.vemerion.minecard.network.GameClient;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.PerformMulliganMessage;
import mod.vemerion.minecard.network.PlayCardMessage;
import mod.vemerion.minecard.network.PlayerChoiceResponseMessage;
import mod.vemerion.minecard.network.SetTutorialStepMessage;
import mod.vemerion.minecard.renderer.CardItemRenderer;
import mod.vemerion.minecard.screen.animation.Animation;
import mod.vemerion.minecard.screen.animation.AttackAnimation;
import mod.vemerion.minecard.screen.animation.BurnAnimation;
import mod.vemerion.minecard.screen.animation.ChargeAnimation;
import mod.vemerion.minecard.screen.animation.DeathAnimation;
import mod.vemerion.minecard.screen.animation.FreezeAnimation;
import mod.vemerion.minecard.screen.animation.HealthAnimation;
import mod.vemerion.minecard.screen.animation.PoisonAnimation;
import mod.vemerion.minecard.screen.animation.StealthAnimation;
import mod.vemerion.minecard.screen.animation.TauntAnimation;
import mod.vemerion.minecard.screen.animation.ThornsAnimation;
import mod.vemerion.minecard.screen.animation.WallAnimation;
import mod.vemerion.minecard.screen.animation.config.AnimationConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderBuffers;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.gui.widget.ExtendedButton;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class GameScreen extends Screen implements GameClient {

	public static final Component TITLE = new TranslatableComponent("gui." + Main.MODID + ".game");
	private static final Component NEXT_TURN = new TranslatableComponent(Helper.gui("next_turn"));
	private static final Component GAME_OVER = new TranslatableComponent(Helper.gui("game_over"));
	private static final Component CHOOSE_TEXT = new TranslatableComponent(Helper.gui("choose"));
	private static final Component MULLIGAN_TEXT = new TranslatableComponent(Helper.gui("mulligan"));
	private static final Component CONFIRM = new TranslatableComponent(Helper.gui("confirm"));

	private static final RenderBuffers RENDER_BUFFERS = new RenderBuffers();

	public static final int CARD_SCALE = 60;
	public static final int CARD_LIGHT = LightTexture.FULL_BRIGHT;
	public static final int CARD_LIGHT_HOVER = 0b011000000000000001100000;
	public static final int CARD_WIDTH = 42;
	public static final int CARD_HEIGHT = 48;
	private static final int NEXT_TURN_BUTTON_SIZE = 20;
	private static final int DECK_HORIZONTAL_OFFSET = 5;
	private static final int DECK_VERTICAL_OFFSET = 55;
	private static final int CARD_PADDING = 4;
	private static final int INFO_BUTTON_SIZE = 12;
	private static final int INFO_BUTTON_X_OFFSET = 15 + INFO_BUTTON_SIZE;
	private static final int INFO_BUTTON_Y_OFFSET = 11 + INFO_BUTTON_SIZE;
	private static final int INFO_SCREEN_PROPERTY_COUNT = 3 * 4;
	private static final int STATS_BUTTON_X_OFFSET = 1 + INFO_BUTTON_SIZE;

	private static final Card EMPTY_CARD = Cards.EMPTY_CARD_TYPE.create();

	// State
	Map<UUID, ClientPlayerState> state;
	private UUID current = UUID.randomUUID();
	private BlockPos pos;
	private boolean isSpectator = true;
	private Choices choices = new Choices();
	private History history = new History();
	private int tutorialStep;
	private List<HistoryEntry> historyList;
	private PlayerStats stats;
	private Map<UUID, String> names;
	private Set<Integer> mulliganTargets = new HashSet<>();

	// Widgets
	private PopupText popup;
	private List<Animation> animations;
	private List<Resources> resources;
	private GameBackground background;
	private Optional<GameTutorial> tutorial = Optional.empty();
	private StatsButton statsButton;

	private Card selectedCard;
	private Card attackingCard;

	public GameScreen(List<MessagePlayerState> list, int tutorialStep, List<HistoryEntry> historyList,
			PlayerStats stats, Map<UUID, String> names, BlockPos pos) {
		super(TITLE);
		this.tutorialStep = tutorialStep;
		this.historyList = historyList;
		this.animations = new ArrayList<>();
		this.state = initState(list);
		this.stats = stats;
		this.names = names;
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
							messageState.resources, messageState.maxResources, messageState.mulligan,
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

		addRenderableWidget(new InfoButton());
		statsButton = addRenderableWidget(new StatsButton());

		if (!isSpectator)
			addRenderableWidget(new NextTurnButton(width - 24, height / 2 - NEXT_TURN_BUTTON_SIZE / 2,
					NEXT_TURN_BUTTON_SIZE, NEXT_TURN_BUTTON_SIZE, TextComponent.EMPTY));

		if (!isSpectator && yourState().mulligan)
			addRenderableWidget(new ExtendedButton(width / 2 - 40, height - 85, 80, 20, CONFIRM, b -> {
				b.visible = false;
				Network.INSTANCE.sendToServer(new PerformMulliganMessage(pos, mulliganTargets));
				tutorial.ifPresent(t -> t.unlock(GameTutorial.UnlockAction.MULLIGAN));
			}));

		if (tutorialStep != -1 && !isSpectator) {
			tutorial = Optional.of(addWidget(new GameTutorial(this, tutorialStep)));
		}
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

		tutorial.ifPresent(t -> t.unlock(GameTutorial.UnlockAction.PLAY_CARD));
	}

	@Override
	public void updateCard(Card received) {
		for (var playerState : state.values()) {
			for (var card : playerState.board) {
				if (card.getId() == received.getId()) {
					var healthChange = received.getHealth() - card.getHealth();
					var old = new HashMap<>(card.getProperties());
					card.copy(received);

					animations.add(new HealthAnimation(minecraft, card, healthChange));

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
		tutorial.ifPresent(t -> t.unlock(GameTutorial.UnlockAction.GAME_OVER));
	}

	@Override
	public void combat(UUID attackerId, int attackerCardId, UUID targetId, int targetCardId) {
		var attacker = withId(state.get(attackerId).board, attackerCardId);
		var target = withId(state.get(targetId).board, targetCardId);

		animations.add(new AttackAnimation(minecraft, attacker, target));
	}

	@Override
	public void setProperties(UUID id, int cardId, Map<ResourceLocation, Integer> properties) {
		var card = withId(state.get(id).board, cardId);
		var old = new HashMap<>(card.getProperties());
		card.getProperties().clear();
		card.getProperties().putAll(properties);
		updatePropertiesAnimations(old, card);
	}

	@Override
	public void playerChoice(Choice choice) {
		choices.set(choice);
	}

	@Override
	public void history(HistoryEntry entry) {
		historyList.add(entry);
	}

	private void updatePropertiesAnimations(Map<ResourceLocation, Integer> old, ClientCard card) {
		for (var entry : card.getProperties().entrySet()) {
			if ((old == null || old.getOrDefault(entry.getKey(), 0) < 1) && entry.getValue() > 0) {
				if (entry.getKey().equals(CardProperty.CHARGE)) {
					animations.add(new ChargeAnimation(minecraft, card, () -> {
					}));
				} else if (entry.getKey().equals(CardProperty.FREEZE)) {
					animations.add(new FreezeAnimation(minecraft, card, () -> {
					}));
				} else if (entry.getKey().equals(CardProperty.SHIELD)) {
					animations.add(new WallAnimation(minecraft, card, () -> {
					}));
				} else if (entry.getKey().equals(CardProperty.STEALTH)) {
					animations.add(new StealthAnimation(minecraft, card, () -> {
					}));
				} else if (entry.getKey().equals(CardProperty.TAUNT)) {
					animations.add(new TauntAnimation(minecraft, card, () -> {
					}));
				} else if (entry.getKey().equals(CardProperty.BURN)) {
					animations.add(new BurnAnimation(minecraft, card, () -> {
					}));
				} else if (entry.getKey().equals(CardProperty.THORNS)) {
					animations.add(new ThornsAnimation(minecraft, card, () -> {
					}));
				} else if (entry.getKey().equals(CardProperty.SPECIAL)) {
				} else if (entry.getKey().equals(CardProperty.BABY)) {
				} else if (entry.getKey().equals(CardProperty.POISON)) {
					animations.add(new PoisonAnimation(minecraft, card, () -> {
					}));
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

	@Override
	public void mulliganDone(UUID id) {
		state.get(id).mulligan = false;
	}

	@Override
	public void stat(PlayerStats.Key key, int value, String name) {
		stats.put(key, value);
		if (!StringUtils.isBlank(name)) {
			names.put(key.getEnemy().get(), name);
		}
		statsButton.clear();
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

	public void setTutorial(int tutorialStep) {
		if (tutorialStep <= this.tutorialStep)
			return;
		this.tutorialStep = tutorialStep;
		Network.INSTANCE.sendToServer(new SetTutorialStepMessage(pos, tutorialStep));
	}

	@Override
	public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
		return tutorial.map(t -> t.mouseReleased(pMouseX, pMouseY, pButton)).orElse(false);
	}

	@Override
	public void mouseMoved(double pMouseX, double pMouseY) {
		tutorial.ifPresent(t -> t.mouseMoved(pMouseX, pMouseY));
	}

	private boolean isMulligan() {
		return state.values().stream().anyMatch(s -> s.mulligan);
	}

	@Override
	public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
		if (super.mouseScrolled(pMouseX, pMouseY, pDelta)) {
			return true;
		}

		for (var playerState : state.values()) {
			for (var list : List.of(playerState.board, playerState.hand)) {
				for (var card : list) {
					if (card.contains(pMouseX, pMouseY)) {
						card.setTextScroll(card.getTextScroll() - (int) Mth.clamp(pDelta, -1, 1));
					}
				}
			}
		}

		return true;
	}

	@Override
	public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
		// Mulligan
		if (!isSpectator && this.yourState().mulligan && pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			for (var card : yourState().hand) {
				if (card.contains(pMouseX, pMouseY)) {
					if (mulliganTargets.contains(card.getId())) {
						mulliganTargets.remove(card.getId());
					} else {
						mulliganTargets.add(card.getId());
					}
					return true;
				}
			}
		}

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
			int x = cardRowX(playerState.hand.size(), enemy ? playerState.hand.size() - i - 1 : i,
					Mth.clamp(10 - playerState.hand.size(), 1, CARD_PADDING));
			int y = enemy ? 5 : height - CARD_HEIGHT - 5;
			playerState.hand.get(i).setPosition(new Vec2(x, y));
		}
		for (int i = 0; i < playerState.board.size(); i++) {
			int x = cardRowX(playerState.board.size(), enemy ? playerState.board.size() - i - 1 : i, CARD_PADDING);
			int y = enemy ? CARD_HEIGHT + 20 : height - CARD_HEIGHT * 2 - 20;
			playerState.board.get(i).setPosition(new Vec2(x, y));
		}
	}

	private int cardRowX(int total, int i, int padding) {
		int cardWidth = CARD_WIDTH + padding;
		return (width - (total - 1) * cardWidth) / 2 + i * cardWidth - CARD_WIDTH / 2;
	}

	private boolean isCurrentActive() {
		return current.equals(minecraft.player.getUUID());
	}

	private PoseStack transformCard(ClientCard card, float yOffset, int mouseX, int mouseY, float partialTicks) {
		var poseStack = new PoseStack();
		if (card.contains(mouseX, mouseY) && selectedCard == null && mouseX > History.X + History.ENTRY_SIZE) {
			var position = card.getPosition(partialTicks);
			float xOffset = 0;
			if (position.x - CARD_WIDTH / 2 < 0)
				xOffset = -(position.x - CARD_WIDTH / 2);
			else if (position.x + CARD_WIDTH * 3 / 2 > width)
				xOffset = width - position.x - CARD_WIDTH * 3 / 2;
			poseStack.translate(-position.x - CARD_WIDTH / 2 + xOffset, -position.y - CARD_HEIGHT / 2 + yOffset, 550);
			poseStack.scale(2, 2, 2);
		}
		return poseStack;
	}

	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float unused) {
		float partialTicks = this.minecraft.getFrameTime();
		var source = RENDER_BUFFERS.bufferSource();

		background.render(poseStack, mouseX, mouseY, source, partialTicks);

		for (var r : resources)
			r.setShaking(0);

		for (var playerState : state.values()) {
			boolean enemy = playerState.isTop;

			// Cards
			if (!isMulligan()) {
				for (var card : playerState.board) {
					if (!card.isDead()) {
						card.render(transformCard(card, 0, mouseX, mouseY, partialTicks), mouseX, mouseY, source,
								partialTicks);
					}
				}
			}
			for (var card : playerState.hand) {
				if (!card.isDead()) {
					card.render(
							!enemy && !isSpectator ? transformCard(card, -CARD_HEIGHT / 2, mouseX, mouseY, partialTicks)
									: new PoseStack(),
							mouseX, mouseY, source, partialTicks);
					if (card.contains(mouseX, mouseY) && !enemy && !isSpectator) {
						for (var r : resources) {
							if (r.id.equals(playerState.id)) {
								r.setShaking(card.getCost());
							}
						}
					}
				}
			}

			// Deck
			float deckX = enemy ? DECK_HORIZONTAL_OFFSET : width - DECK_HORIZONTAL_OFFSET * 2.3f;
			float deckY = enemy ? DECK_VERTICAL_OFFSET : height - DECK_VERTICAL_OFFSET - CARD_HEIGHT;
			float offset = enemy ? 1 : -1;
			for (int i = 0; i < playerState.deck; i++) {
				poseStack.pushPose();
				poseStack.translate(deckX + i * offset, deckY, 100);
				poseStack.mulPose(new Quaternion(0, 80 * offset, 0, true));
				poseStack.scale(1f, 1f, 0.2f);

				new ClientCard(EMPTY_CARD, Vec2.ZERO, this).render(poseStack, 0, 0, source, partialTicks);
				poseStack.popPose();
			}
			if (mouseX > (enemy ? deckX - 3 : deckX - playerState.deck * 1)
					&& mouseX < (enemy ? deckX + playerState.deck + 7 : deckX + 7) && mouseY > deckY
					&& mouseY < deckY + CARD_HEIGHT)
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

		// Mulligan
		if (!isSpectator && yourState().mulligan) {
			var yourState = yourState();
			for (var card : yourState.hand)
				if (mulliganTargets.contains(card.getId()))
					drawBarrier(poseStack, source, card);

			// Text
			poseStack.pushPose();
			poseStack.translate((width - font.width(CHOOSE_TEXT) * 1.5f) / 2, height - 100, 20);
			poseStack.scale(1.5f, 1.5f, 1.5f);
			font.drawShadow(poseStack, MULLIGAN_TEXT, 0, 0, 0xffffff);
			poseStack.popPose();
		}

		for (var r : resources)
			r.render(new PoseStack(), mouseX, mouseY, partialTicks, source);

		source.endBatch(); // Fixes problems with rendering semi-transparent animations

		for (var animation : animations)
			animation.render(mouseX, mouseY, source, partialTicks);

		choices.render(poseStack, mouseX, mouseY, source, partialTicks);
		history.render(poseStack, mouseX, mouseY, source, partialTicks);

		popup.render(poseStack, mouseX, mouseY, partialTicks);

		tutorial.ifPresent(t -> t.render(poseStack, mouseX, mouseY, source, partialTicks));

		source.endBatch();

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
		history.tick();

		tutorial.ifPresent(t -> t.tick());
	}

	private static final Method GET_AMBIENT_SOUND = ObfuscationReflectionHelper.findMethod(Mob.class, "m_7515_");

	private void playAmbientSound(Card card) {
		if (CardItemRenderer.getEntity(card, minecraft.level) instanceof Mob entity) {
			try {
				var sound = GET_AMBIENT_SOUND.invoke(entity) instanceof SoundEvent deathSound ? deathSound
						: SoundEvents.ITEM_FRAME_ADD_ITEM;
				minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				Main.LOGGER.error("Unable to play ambient sound for card "
						+ card.getType().get().getRegistryName().toString() + ": " + e);
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
			if (isCurrentActive() && choices.isEmpty() && !isMulligan()) {
				Network.INSTANCE.sendToServer(new EndTurnMessage(pos));
				selectedCard = null;
				attackingCard = null;
				tutorial.ifPresent(t -> t.unlock(GameTutorial.UnlockAction.END_TURN));
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

	private abstract class OverlayButton extends AbstractButton {

		private static final TranslatableComponent BUTTON_HOVER = new TranslatableComponent(Helper.gui("button_hover"));

		private int page = 0;
		private boolean isHovered0;
		private long hoverTimestamp;
		private ResourceLocation texture;

		public OverlayButton(int x, int y, ResourceLocation texture) {
			super(x, y, INFO_BUTTON_SIZE, INFO_BUTTON_SIZE, TextComponent.EMPTY);
			this.texture = texture;
		}

		@Override
		public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

		}

		@Override
		public void onPress() {
			page = (page + 1) % maxPages();
		}

		@Override
		public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
			if (isHovered && !isHovered0) {
				hoverTimestamp = minecraft.level.getGameTime();
			}
			isHovered0 = isHovered;
			RenderSystem.enableDepthTest();

			if (isHovered) {
				pPoseStack.pushPose();
				pPoseStack.translate(0, 0, 500);
				GuiComponent.fill(pPoseStack, 0, 0, GameScreen.this.width, GameScreen.this.height, 0xcc000000);
				if (maxPages() > 1 && (minecraft.level.getGameTime() - hoverTimestamp < 60))
					GameScreen.this.renderTooltip(pPoseStack, BUTTON_HOVER, pMouseX, pMouseY);
				pPoseStack.popPose();
			}

			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderTexture(0, texture);

			RenderSystem.setShaderColor(isHovered ? 0.6f : 1, isHovered ? 0.6f : 1, 1, 1);
			blit(pPoseStack, x, y, 0, 0, width, height, INFO_BUTTON_SIZE, INFO_BUTTON_SIZE);
			if (isHovered) {
				drawOverlay(pPoseStack, pMouseX, pMouseY, pPartialTick, page);
			}
		}

		protected abstract int maxPages();

		protected abstract void drawOverlay(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick,
				int page);
	}

	private class InfoButton extends OverlayButton {

		private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/info.png");

		public InfoButton() {
			super(GameScreen.this.width - INFO_BUTTON_X_OFFSET, GameScreen.this.height / 2 - INFO_BUTTON_Y_OFFSET,
					TEXTURE);
		}

		@Override
		protected int maxPages() {
			return 1 + (int) Math.max(0, CardProperties.getInstance(true).entries().stream()
					.filter(e -> !e.getValue().getItem().isEmpty()).count() - 1) / INFO_SCREEN_PROPERTY_COUNT;
		}

		@Override
		protected void drawOverlay(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick, int page) {
			poseStack.pushPose();
			poseStack.translate(0, 0, 500);

			int border = 80;
			int padding = (GameScreen.this.width - border * 2) / 2;
			int i = 0;
			long skip = page * INFO_SCREEN_PROPERTY_COUNT;
			for (var entry : CardProperties.getInstance(true).entries()) {
				if (entry.getValue().getItem().isEmpty())
					continue;
				skip--;
				if (skip > 0)
					continue;
				if (i == INFO_SCREEN_PROPERTY_COUNT)
					break;

				var property = entry.getValue();
				var rl = entry.getKey();

				int x = padding * (i % 3) + border - 16 / 2;
				int y = i / 3 * 60 + 1;

				poseStack.pushPose();
				poseStack.translate(x + 8, y + 8, 50);
				poseStack.scale(16, -16, 1);
				itemRenderer.renderStatic(property.getItem(), ItemTransforms.TransformType.GUI,
						LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, RENDER_BUFFERS.bufferSource(),
						0);
				poseStack.popPose();

				// Title
				float size = 1.3f;
				var title = new TranslatableComponent(CardProperty.getTextKey(rl));
				var width = font.width(title) * size;
				poseStack.pushPose();
				poseStack.translate(x + 8 - width / 2, y + 17, 0);
				poseStack.scale(size, size, size);
				font.drawShadow(poseStack, title, 0, 0, 0xffffffff);
				poseStack.popPose();

				// Description
				var description = new TranslatableComponent(CardProperty.getDescriptionKey(rl));
				var lines = font.split(description, 110);
				for (int j = 0; j < lines.size(); j++) {
					font.drawShadow(poseStack, lines.get(j), x + 8 - font.width(lines.get(j)) / 2, y + 30 + j * 9,
							0xffffffff);
				}
				i++;
			}

			// Page count
			var pageText = new TextComponent((page + 1) + "/" + maxPages());
			font.drawShadow(poseStack, pageText, 4, GameScreen.this.height - 12, 0xffaaaaaa);

			poseStack.popPose();
		}

	}

	private class StatsButton extends OverlayButton {

		private static final int SEPARATOR_OFFSET = 10;
		private static final int STATS_START = 30;
		private static final int SPACING = 10;
		private static final int ENEMIES_PER_PAGE = 4;

		private static final Component GENERAL_HEADER = new TranslatableComponent(Helper.gui("stats_general"));
		private static final Component ENEMIES_HEADER = new TranslatableComponent(Helper.gui("stats_enemies"));

		private static final ResourceLocation TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/stats.png");

		private List<PlayerStats.StatLine> general;
		private Map<UUID, List<StatLine>> enemies;

		public StatsButton() {
			super(GameScreen.this.width - STATS_BUTTON_X_OFFSET, GameScreen.this.height / 2 - INFO_BUTTON_Y_OFFSET,
					TEXTURE);
		}

		private void clear() {
			general = null;
			enemies = null;
		}

		@Override
		protected void drawOverlay(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick, int page) {
			if (general == null || enemies == null) {
				general = stats.getGeneral();
				enemies = stats.getEnemies();
			}

			pPoseStack.pushPose();
			pPoseStack.translate(0, 0, 500);

			drawHeader(pPoseStack, GENERAL_HEADER, 0, 1.5f);
			drawHeader(pPoseStack, ENEMIES_HEADER, GameScreen.this.width / 2, 1.5f);

			// General
			for (int i = 0; i < general.size(); i++) {
				drawStatLine(pPoseStack, general.get(i), SPACING, STATS_START + i * 15,
						i % 2 == 0 ? 0xffffffff : 0xffbbbbbb, GameScreen.this.width / 2 - SPACING * 2);
			}

			// Enemies
			int i = -ENEMIES_PER_PAGE * page;
			int enemyHeight = (GameScreen.this.height - STATS_START) / ENEMIES_PER_PAGE;
			for (var entry : enemies.entrySet()) {
				if (i < 0) {
					i++;
					continue;
				} else if (i >= ENEMIES_PER_PAGE) {
					break;
				}
				font.drawShadow(pPoseStack, getPlayerName(entry.getKey()), GameScreen.this.width / 2 + SPACING,
						STATS_START + i * enemyHeight, 0xffffffff);
				int j = 1;
				for (var line : entry.getValue()) {
					drawStatLine(pPoseStack, line, GameScreen.this.width / 2 + SPACING * 2,
							STATS_START + i * enemyHeight + j * 15, j % 2 == 0 ? 0xffbbbbbb : 0xff888888,
							GameScreen.this.width / 2 - SPACING * 3);
					j++;
				}
				i++;
			}
			vLine(pPoseStack, GameScreen.this.width / 2, SEPARATOR_OFFSET, GameScreen.this.height - SEPARATOR_OFFSET,
					0xffffffff);

			// Page count
			var pageText = new TextComponent((page + 1) + "/" + maxPages());
			font.drawShadow(pPoseStack, pageText, 4, GameScreen.this.height - 12, 0xffaaaaaa);

			pPoseStack.popPose();
		}

		private void drawStatLine(PoseStack poseStack, PlayerStats.StatLine line, int x, int y, int color, int width) {
			font.drawShadow(poseStack, line.component(), x, y, color);
			font.drawShadow(poseStack, line.value(), x + width - font.width(line.value()), y, color);
		}

		private Component getPlayerName(UUID id) {
			if (AIPlayer.isAi(id)) {
				return new TranslatableComponent(ModEntities.CARD_GAME_ROBOT.get().getDescriptionId());
			}

			return new TextComponent(names.getOrDefault(id, id.toString()));
		}

		private void drawHeader(PoseStack poseStack, Component text, int x, float scale) {
			poseStack.pushPose();
			poseStack.translate(x + (GameScreen.this.width / 2 - font.width(text) * scale) / 2, SEPARATOR_OFFSET, 0);
			poseStack.scale(scale, scale, scale);
			font.drawShadow(poseStack, text, 0, 0, 0xffffffff);
			poseStack.popPose();
		}

		@Override
		protected int maxPages() {
			return enemies == null || enemies.isEmpty() ? 1
					: (int) Math.ceil((double) enemies.size() / ENEMIES_PER_PAGE);
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
		private int shaking;

		private float[] scales = new float[20];
		private float[] scales0 = new float[20];

		private Resources(UUID id, boolean top) {
			this.id = id;
			this.top = top;
		}

		public void setShaking(int i) {
			shaking = i;
		}

		public void render(PoseStack poseStack, int pMouseX, int pMouseY, float pPartialTick, BufferSource source) {
			var position = new Vec2((top ? 200 : width - 200), top ? 60 : height - 60);
			var playerState = state.get(id);

			for (int i = 0; i < 10; i++) {
				poseStack.pushPose();
				poseStack.translate(position.x + i * OFFSET * (top ? -1 : 1), position.y, 0);
				if (shaking > 0 && shaking <= playerState.resources && i >= playerState.resources - shaking
						&& i < playerState.resources) {
					poseStack.mulPose(new Quaternion(0, 0,
							Mth.cos(minecraft.level.getGameTime() % 100000 + pPartialTick) * 20, true));
				}

				renderResource(poseStack, pPartialTick, source, i * 2, true);
				renderResource(poseStack, pPartialTick, source, i * 2 + 1, false);

				poseStack.popPose();
			}

			// Render count text
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
		private Choice choice;
		private List<ClientCard> cards = new ArrayList<>();

		private void set(Choice choice) {
			this.choice = choice;
			update();
		}

		private void clear() {
			choice = null;
			cards.clear();
			update();
		}

		private void update() {
			cards = new ArrayList<>();
			if (!isEmpty() && !choice.targeting()) {
				var selectable = choice.cards();
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
			return choice == null;
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

				var lines = font.split(choice.ability().getText(), 200);
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

			if (choice.targeting()) { // Select target on board/in hand
				for (var playerState : state.values()) {
					for (var list : List.of(playerState.board, playerState.hand)) {
						for (var card : list) {
							if (card.contains(pMouseX, pMouseY)
									&& choice.cards().stream().anyMatch(c -> c.getId() == card.getId())) {
								Network.INSTANCE.sendToServer(new PlayerChoiceResponseMessage(pos, card.getId()));
								clear();
								return true;
							}
						}
					}
				}
			} else { // Select choice from sent cards
				for (var card : cards) {
					if (card.contains(pMouseX, pMouseY)) {
						Network.INSTANCE.sendToServer(new PlayerChoiceResponseMessage(pos, card.getId()));
						clear();
						return true;
					}
				}
			}
			return false;
		}
	}

	private class History {
		private static final int ENTRY_SIZE = 10;
		private static final int PADDING = 5;
		private static final int BORDER = 1;
		private static final int X = 10;
		private static final int TOTAL_SIZE = ENTRY_SIZE + PADDING;
		private static final int MAX_ENTRIES = 15;
		private static final int ENTRY_CARD_SCALE = 2;
		private static final int TARGETS_SPACING = 5;
		private static final int CARD_TARGET_SPACING = 10;

		private float xPos = X;
		private float xPos0 = xPos;
		private float mouseX;

		private void tick() {
			xPos0 = xPos;
			if (mouseX < History.X + History.ENTRY_SIZE)
				xPos = Mth.clampedLerp(xPos, X, 0.4f);
			else
				xPos = Mth.clamp(xPos, -X - ENTRY_SIZE, 0.4f);
		}

		private int getX(float partialTicks) {
			return (int) Mth.lerp(partialTicks, xPos0, xPos);
		}

		private void render(PoseStack poseStack, int mouseX, int mouseY, BufferSource source, float partialTick) {
			this.mouseX = mouseX;
			int y = (int) (height / 2 + ENTRY_SIZE / 2
					- (Math.min(MAX_ENTRIES, historyList.size()) - 1) / 2f * TOTAL_SIZE);
			int x = getX(partialTick);
			for (int i = historyList.size() - 1; i >= Math.max(0, historyList.size() - MAX_ENTRIES); i--) {
				var entry = historyList.get(i);
				var entity = CardItemRenderer.getEntity(entry.getCard(), minecraft.level);
				poseStack.pushPose();
				poseStack.translate(x, y + (entity.getType() == EntityType.ITEM ? 6 : 0), 500);
				float scale = getScale(entity);
				poseStack.scale(scale, -scale, scale);
				poseStack.mulPose(new Quaternion(0, 20, 0, true));
				minecraft.getEntityRenderDispatcher().getRenderer(entity).render(entity, 0, 0, poseStack, source,
						LightTexture.FULL_BRIGHT);
				poseStack.popPose();
				poseStack.pushPose();
				poseStack.translate(0, 0, 600);
				int color = state.get(entry.getPlayerId()).isTop ? 0xffff2020 : 0xff00ff70;
				fill(poseStack, x - ENTRY_SIZE / 2, y, x + ENTRY_SIZE / 2, y + BORDER, color); // Bottom
				fill(poseStack, x - ENTRY_SIZE / 2, y - ENTRY_SIZE - BORDER, x + ENTRY_SIZE / 2, y - ENTRY_SIZE, color); // Top
				fill(poseStack, x - ENTRY_SIZE / 2 - BORDER, y, x - ENTRY_SIZE / 2, y - ENTRY_SIZE, color); // Left
				fill(poseStack, x + ENTRY_SIZE / 2, y, x + ENTRY_SIZE / 2 + BORDER, y - ENTRY_SIZE, color); // Right
				poseStack.popPose();

				if (mouseX > x - ENTRY_SIZE / 2 && mouseX < x + ENTRY_SIZE / 2 && mouseY > y - ENTRY_SIZE
						&& mouseY < y) {
					renderCards(entry, y, x, poseStack, source, partialTick);
				}

				y += TOTAL_SIZE;
			}
		}

		private void renderCards(HistoryEntry entry, int y, int x, PoseStack poseStack, BufferSource source,
				float partialTick) {
			y = Mth.clamp(y - CARD_HEIGHT / 2 * ENTRY_CARD_SCALE - ENTRY_SIZE / 2, 0,
					height - CARD_HEIGHT * ENTRY_CARD_SCALE);
			var card = new ClientCard(entry.getCard(), Vec2.ZERO, GameScreen.this);
			poseStack.pushPose();
			poseStack.translate(x + ENTRY_SIZE / 2 + 3, y, 500);
			poseStack.scale(ENTRY_CARD_SCALE, ENTRY_CARD_SCALE, ENTRY_CARD_SCALE);
			card.render(poseStack, 0, 0, source, partialTick);
			poseStack.popPose();

			renderTargets(entry, y, x, entry.getIcon(), poseStack, source, partialTick);
		}

		private void renderTargets(HistoryEntry entry, int y, int x, ItemStack stack, PoseStack poseStack,
				BufferSource source, float partialTick) {
			// Targets
			if (!entry.getTargets().isEmpty()) {
				var targets = entry.getTargets();
				float xStart = x + ENTRY_SIZE / 2 + 3 + CARD_WIDTH * ENTRY_CARD_SCALE + CARD_TARGET_SPACING;

				// Extra space to make sure scaled down card is not hidden behind item
				if (targets.size() > 8)
					xStart += CARD_TARGET_SPACING;

				// Spacing is also scaled down
				float targetScale = Mth.clamp((width - xStart) / (targets.size() * (CARD_WIDTH + TARGETS_SPACING)),
						0.3f, ENTRY_CARD_SCALE);
				float yStart = y + (ENTRY_CARD_SCALE - targetScale) * CARD_HEIGHT / 2;
				for (int i = 0; i < targets.size(); i++) {
					var target = new ClientCard(targets.get(i).card(), Vec2.ZERO, GameScreen.this);
					poseStack.pushPose();
					poseStack.translate(xStart + i * (targetScale * (CARD_WIDTH + TARGETS_SPACING)), yStart, 500);
					poseStack.scale(targetScale, targetScale, targetScale);
					target.render(poseStack, 0, 0, source, partialTick);
					poseStack.popPose();
				}
			}

			// Item
			poseStack.pushPose();
			poseStack.translate(x + ENTRY_SIZE / 2 + 3 + CARD_WIDTH * ENTRY_CARD_SCALE + CARD_TARGET_SPACING / 2,
					y + CARD_HEIGHT / 2 * ENTRY_CARD_SCALE, 600);
			poseStack.scale(40, -40, 40);
			itemRenderer.renderStatic(stack, ItemTransforms.TransformType.GUI, LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, poseStack, source, 0);
			poseStack.popPose();
		}

		private float getScale(Entity entity) {
			if (entity.getType() == EntityType.ITEM) {
				return 25;
			}

			var dimensions = entity.getType().getDimensions();
			return Math.min(ENTRY_SIZE / dimensions.width, ENTRY_SIZE / dimensions.height);
		}
	}
}
