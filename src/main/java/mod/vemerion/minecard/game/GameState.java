package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.ability.CardAbilityTrigger;
import mod.vemerion.minecard.network.CombatMessage;
import mod.vemerion.minecard.network.HistoryMessage;
import mod.vemerion.minecard.network.PlaceCardMessage;
import mod.vemerion.minecard.network.UpdateCardsMessage;
import mod.vemerion.minecard.network.UpdateDecksMessage;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class GameState {

	public static final Codec<GameState> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(Codec.list(PlayerState.CODEC).fieldOf("playerStates").forGetter(GameState::getPlayerStates),
							Codec.INT.fieldOf("turn").forGetter(GameState::getTurn),
							Codec.INT.fieldOf("tutorialStep").forGetter(GameState::getTutorialStep),
							Codec.list(HistoryEntry.CODEC).fieldOf("history").forGetter(GameState::getHistory))
					.apply(instance, GameState::new)));

	public static final int MAX_HAND_SIZE = 10;
	public static final int MAX_BOARD_SIZE = 8;
	public static final int MAX_HISTORY_SIZE = 20;

	private List<PlayerState> playerStates;
	private int turn;
	private int tutorialStep;
	private List<HistoryEntry> history;
	private Random random;
	private PlayerChoice choice = new PlayerChoice();
	private Level level;

	public GameState(List<PlayerState> playerStates, int turn, int tutorialStep, List<HistoryEntry> history) {
		this.playerStates = new ArrayList<>(playerStates);
		this.turn = turn;
		this.tutorialStep = tutorialStep;
		this.history = new ArrayList<>(history);
		random = new Random();

		for (var playerState : playerStates)
			playerState.setGame(this);
	}

	public GameState() {
		this(new ArrayList<>(), 0, -1, new ArrayList<>());
	}

	public Random getRandom() {
		return random;
	}

	public List<PlayerState> getPlayerStates() {
		return playerStates;
	}

	public int getTurn() {
		return turn;
	}

	public int getTutorialStep() {
		return tutorialStep;
	}

	public List<HistoryEntry> getHistory() {
		return history;
	}

	public void addHistory(List<Receiver> receivers, HistoryEntry entry) {
		history.add(entry);

		if (history.size() > MAX_HISTORY_SIZE)
			history.remove(0);

		for (var receiver : receivers) {
			receiver.receiver(new HistoryMessage(entry.censor(receiver.getId(), isSpectator(receiver.getId()))));
		}
	}

	public boolean isSpectator(UUID id) {
		for (var playerState : playerStates) {
			if (playerState.getId().equals(id))
				return false;
		}
		return true;
	}

	public PlayerChoice getChoice() {
		return choice;
	}

	public boolean isTutorial() {
		return tutorialStep != -1;
	}

	public void setTutorialStep(int step) {
		if (step == -1)
			return;
		tutorialStep = step;
	}

	public boolean isInBoard(Card card) {
		for (var playerState : playerStates)
			for (var c : playerState.getBoard())
				if (c.getId() == card.getId())
					return true;
		return false;
	}

	public PlayerState getYourPlayerState(UUID id) {
		for (var playerState : playerStates)
			if (playerState.getId().equals(id))
				return playerState;
		return null;
	}

	public PlayerState getEnemyPlayerState(UUID id) {
		for (var playerState : playerStates)
			if (!playerState.getId().equals(id))
				return playerState;
		return null;
	}

	public UUID getCurrentPlayer() {
		return getCurrentPlayerState().getId();
	}

	public PlayerState getCurrentPlayerState() {
		return playerStates.get(turn % 2);
	}

	private PlayerState getEnemyPlayerState() {
		return playerStates.get((turn + 1) % 2);
	}

	public CardVisibility calcVisibility(UUID playerId, Card card) {
		for (var playerState : getPlayerStates()) {
			for (var c : playerState.getDeck()) {
				if (c.getId() == card.getId())
					return CardVisibility.DECK;
			}
			for (var c : playerState.getHand()) {
				if (c.getId() == card.getId())
					return playerId.equals(playerState.getId()) ? CardVisibility.VISIBLE : CardVisibility.ENEMY_HAND;
			}
			for (var c : playerState.getBoard())
				if (c.getId() == card.getId())
					return CardVisibility.VISIBLE;
		}
		return CardVisibility.UNKNOWN;
	}

	public void updateCards(Receiver receiver, Iterable<Card> cards) {
		boolean updateDeckSizes = false;
		List<Card> updated = new ArrayList<>();
		for (var card : cards) {
			var visibility = calcVisibility(receiver.getId(), card);
			switch (visibility) {
			case DECK:
				updateDeckSizes = true;
				break;
			case ENEMY_HAND:
				if (card.isDead()) {
					var fake = Cards.EMPTY_CARD_TYPE.create().setId(card.getId());
					fake.setHealth(0);
					updated.add(fake);
				}
				break;
			case UNKNOWN:
				break;
			case VISIBLE:
				updated.add(card);
				break;

			}
		}

		receiver.receiver(new UpdateCardsMessage(updated));

		if (updateDeckSizes) {
			updateDecks(List.of(receiver));
		}
	}

	public void updateDecks(List<Receiver> receivers) {
		var msg = new UpdateDecksMessage(playerStates.stream()
				.map(s -> Pair.of(s.getId(), (int) s.getDeck().stream().filter(c -> !c.isDead()).count()))
				.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue())));
		for (var receiver : receivers)
			receiver.receiver(msg);
	}

	public void hurt(List<Receiver> receivers, Card card, int amount) {
		if (card.isDead())
			return;

		var tookDamage = card.hurt(amount);

		PlayerState owner = null;
		List<Card> container = null;
		List<Card> graveyard = null;

		for (var playerState : getPlayerStates()) {
			for (var list : List.of(playerState.getBoard(), playerState.getHand(), playerState.getDeck())) {
				if (list.stream().anyMatch(c -> c.getId() == card.getId())) {
					owner = playerState;
					container = list;
					graveyard = owner.getGraveyard();
					break;
				}
			}
		}

		if (owner == null || container == null)
			return;

		final var playerState = owner;
		if (tookDamage) {
			card.ability(a -> a.trigger(CardAbilityTrigger.HURT, receivers, playerState, card, card, null));
		}

		if (card.isDead()) {
			card.ability(a -> a.trigger(CardAbilityTrigger.DEATH, receivers, playerState, card, card, null));
		}

		for (var receiver : receivers) {
			updateCards(receiver, List.of(card));
		}

		if (card.isDead()) {
			container.remove(card);
			graveyard.add(card);
		}
	}

	public void heal(List<Receiver> receivers, Card card, int amount) {
		var healthBefore = card.getHealth();
		card.setHealth(Math.min(card.getMaxHealth(), card.getHealth() + amount));

		if (healthBefore != card.getHealth()) {
			for (var receiver : receivers) {
				updateCards(receiver, List.of(card));
			}
		}
	}

	public boolean summonCard(List<Receiver> receivers, Card card, UUID id, int leftId) {
		var playerState = getYourPlayerState(id);
		var board = playerState.getBoard();

		if (board.size() >= MAX_BOARD_SIZE)
			return false;

		if (leftId == -1) {
			board.add(0, card);
		} else {
			for (var i = 0; i < board.size(); i++) {
				if (board.get(i).getId() == leftId) {
					board.add(i + 1, card);
					break;
				}
			}
		}

		for (var receiver : receivers) {
			receiver.receiver(new PlaceCardMessage(id, card, leftId));
		}

		card.ability(a -> a.trigger(CardAbilityTrigger.SUMMON, receivers, playerState, card, card, null));
		return true;
	}

	public void endTurn(List<Receiver> receivers) {
		getCurrentPlayerState().endTurn(receivers);
		turn++;
		getCurrentPlayerState().newTurn(receivers);
	}

	public void attack(List<Receiver> receivers, int attackerId, int targetId) {
		var current = getCurrentPlayerState();
		var enemy = getEnemyPlayerState();

		var attackerCard = current.findFromBoard(attackerId);
		var targetCard = enemy.findFromBoard(targetId);

		if (attackerCard == null || targetCard == null || !attackerCard.canAttack())
			return;

		// Can't be targeted
		if (!GameUtil.canBeAttacked(targetCard, enemy.getBoard())) {
			return;
		}

		attackerCard.ability(
				a -> a.trigger(CardAbilityTrigger.ATTACK, receivers, current, attackerCard, attackerCard, targetCard));

		attackerCard.removeProperty(CardProperty.STEALTH);
		hurt(receivers, attackerCard, targetCard.getDamage() + targetCard.getProperty(CardProperty.THORNS));
		hurt(receivers, targetCard, attackerCard.getDamage());

		for (var receiver : receivers) {
			receiver.receiver(
					new CombatMessage(current.getId(), attackerCard.getId(), enemy.getId(), targetCard.getId()));
		}

		addHistory(receivers, new HistoryEntry(new ItemStack(Items.NETHERITE_SWORD), current.getId(), attackerCard,
				List.of(targetCard.toHistory(HistoryEntry.Visibility.ALL))));

		for (var playerState : getPlayerStates()) {
			for (var card : playerState.getBoard()) {
				if (card != attackerCard) {
					card.ability(a -> a.trigger(CardAbilityTrigger.OTHER_ATTACK_POST, receivers, playerState, card,
							attackerCard, targetCard));
				}
			}
		}
	}

	public void choice(List<Receiver> receivers, int selected) {
		getChoice().respond(receivers, selected);
	}

	// Completely remove the card without running onDeath
	public void removeCard(List<Receiver> receivers, Card card) {
		card.setHealth(-1);
		for (var receiver : receivers)
			updateCards(receiver, List.of(card));
		for (var playerState : playerStates) {
			playerState.getHand().remove(card);
			playerState.getBoard().remove(card);
			playerState.getDeck().remove(card);
		}
	}

	public boolean isGameOver() {
		return playerStates.stream().anyMatch(s -> s.isGameOver());
	}

	public boolean isMulligan() {
		return playerStates.stream().anyMatch(s -> s.isMulligan());
	}

	public void setLevel(Level level) {
		this.level = level;
	}

	public Level getLevel() {
		return level;
	}
}
