package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.capability.PlayerStats;
import mod.vemerion.minecard.capability.StatsData;
import mod.vemerion.minecard.game.ability.CardAbilityTrigger;
import mod.vemerion.minecard.network.DrawCardsMessage;
import mod.vemerion.minecard.network.MulliganDoneMessage;
import mod.vemerion.minecard.network.PlaceCardMessage;
import mod.vemerion.minecard.network.SetResourcesMessage;
import net.minecraft.core.SerializableUUID;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public class PlayerState {

	public static final Codec<PlayerState> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder
							.create(instance -> instance
									.group(SerializableUUID.CODEC.fieldOf("id").forGetter(PlayerState::getId),
											Codec.list(Card.CODEC).fieldOf("deck").forGetter(PlayerState::getDeck),
											Codec.list(Card.CODEC).fieldOf("hand").forGetter(PlayerState::getHand),
											Codec.list(Card.CODEC).fieldOf("board").forGetter(PlayerState::getBoard),
											Codec.list(Card.CODEC).fieldOf("graveyard")
													.forGetter(PlayerState::getGraveyard),
											Codec.INT.fieldOf("resources").forGetter(PlayerState::getResources),
											Codec.INT.fieldOf("maxResources").forGetter(PlayerState::getMaxResources),
											Codec.BOOL.fieldOf("mulligan").forGetter(PlayerState::isMulligan))
									.apply(instance, PlayerState::new)));

	private UUID id;
	private List<Card> deck;
	private List<Card> hand;
	private List<Card> board;
	private List<Card> graveyard;
	private int resources;
	private int maxResources;
	private boolean mulligan;
	private GameState game;
	private boolean isGameOver;

	public PlayerState(UUID id, List<Card> deck, List<Card> hand, List<Card> board, List<Card> graveyard, int resources,
			int maxResources, boolean mulligan) {
		this.id = id;
		this.deck = new ArrayList<>(deck);
		this.hand = new ArrayList<>(hand);
		this.board = new ArrayList<>(board);
		this.graveyard = new ArrayList<>(graveyard);
		this.resources = resources;
		this.maxResources = maxResources;
		this.mulligan = mulligan;
	}

	public void setGame(GameState game) {
		this.game = game;
	}

	public GameState getGame() {
		return game;
	}

	public UUID getId() {
		return id;
	}

	public List<Card> getDeck() {
		return deck;
	}

	public List<Card> getHand() {
		return hand;
	}

	public List<Card> getBoard() {
		return board;
	}

	public List<Card> getGraveyard() {
		return graveyard;
	}

	public int getResources() {
		return resources;
	}

	public int getMaxResources() {
		return maxResources;
	}

	public boolean isMulligan() {
		return mulligan;
	}

	private Card withId(List<Card> list, int id) {
		return list.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
	}

	public Card findFromBoard(int id) {
		return withId(board, id);
	}

	public Card findFromHand(int id) {
		return withId(hand, id);
	}

	public List<Card> drawCards(List<Receiver> receivers, int count) {
		List<Card> cards = new ArrayList<>();
		List<Card> fakes = new ArrayList<>();
		while (!deck.isEmpty() && count > 0 && hand.size() < GameState.MAX_HAND_SIZE) {
			var card = deck.remove(deck.size() - 1);
			hand.add(card);
			cards.add(card);
			fakes.add(Cards.EMPTY_CARD_TYPE.create().setId(card.getId()));
			count--;
		}

		for (var receiver : receivers) {
			receiver.receiver(new DrawCardsMessage(id, receiver.getId().equals(id) ? cards : fakes, true));
		}
		return cards;
	}

	public void addCards(List<Receiver> receivers, List<Card> cards) {
		while (!cards.isEmpty() && hand.size() + cards.size() > GameState.MAX_HAND_SIZE) {
			cards.remove(cards.size() - 1);
		}

		List<Card> fakes = new ArrayList<>();
		for (var card : cards)
			fakes.add(Cards.EMPTY_CARD_TYPE.create().setId(card.getId()));

		hand.addAll(cards);

		for (var receiver : receivers) {
			receiver.receiver(new DrawCardsMessage(id, receiver.getId().equals(id) ? cards : fakes, false));
		}
	}

	public void endTurn(List<Receiver> receivers) {
		for (var card : new ArrayList<>(new ArrayList<>(board))) {
			card.ability(a -> a.trigger(CardAbilityTrigger.TICK, receivers, this, card, null));
		}
	}

	public void newTurn(List<Receiver> receivers) {
		maxResources = Math.min(10, maxResources + 1);
		resources = maxResources;

		Set<Card> updated = new HashSet<>();
		for (var card : new ArrayList<>(board)) {
			if (card.hasProperty(CardProperty.BABY)) {
				card.decrementProperty(CardProperty.BABY);
				if (!card.hasProperty(CardProperty.BABY)) {
					card.ability(a -> a.trigger(CardAbilityTrigger.GROW, receivers, this, card, null));
				}
				updated.add(card);
			}
			if (card.hasProperty(CardProperty.FREEZE)) {
				card.decrementProperty(CardProperty.FREEZE);
				updated.add(card);
			} else {
				card.putProperty(CardProperty.READY, card.getProperty(CardProperty.ECHO) + 1);
				updated.add(card);
			}
		}

		if (!updated.isEmpty())
			for (var receiver : receivers)
				game.updateCards(receiver, updated);

		drawCards(receivers, 1);
	}

	public void addResources(List<Receiver> receivers, int temporaryResources, int permanentResources) {
		resources = Mth.clamp(resources + temporaryResources, 0, 10);
		maxResources = Mth.clamp(maxResources + permanentResources, 0, 10);

		var msg = new SetResourcesMessage(id, resources, maxResources);

		for (var receiver : receivers)
			receiver.receiver(msg);
	}

	public void performMulligan(List<Receiver> receivers, Set<Integer> cards) {
		if (!isMulligan())
			return;

		mulligan = false;
		List<Card> discarded = new ArrayList<>();
		for (var c : cards) {
			var card = findFromHand(c);
			if (card == null)
				continue;
			discarded.add(new Card(card));
			game.removeCard(receivers, card);
		}

		drawCards(receivers, discarded.size());

		for (var card : discarded)
			deck.add(game.getRandom().nextInt(deck.size()), card);

		game.updateDecks(receivers);

		var msg = new MulliganDoneMessage(id);
		for (var receiver : receivers) {
			receiver.receiver(msg);
		}
	}

	public void playCard(List<Receiver> receivers, int cardId, int leftId) {
		var card = findFromHand(cardId);
		var left = findFromBoard(leftId);
		if (card == null || (leftId != -1 && left == null))
			return;

		if (card.getCost() > resources || (board.size() >= GameState.MAX_BOARD_SIZE && !card.isSpell()))
			return;

		resources -= card.getCost();

		if (!card.isSpell()) {
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
		}

		hand.remove(card);

		for (var receiver : receivers) {
			receiver.receiver(new SetResourcesMessage(id, resources, maxResources));
			receiver.receiver(new PlaceCardMessage(id, card, leftId));
		}

		if (!card.isSpell())
			game.addHistory(receivers, new HistoryEntry(ItemStack.EMPTY, id, card, List.of()));

		card.ability(a -> a.trigger(CardAbilityTrigger.SUMMON, receivers, this, card, null));

		StatsData.inc(game.getLevel(), id, PlayerStats.Key.CARDS_PLAYED, Optional.empty());
	}

	public void shuffleIn(List<Receiver> receivers, List<Card> cards) {
		for (var card : cards) {
			if (deck.isEmpty()) {
				deck.add(card);
			} else {
				deck.add(game.getRandom().nextInt(deck.size()), card);
			}
		}
		for (var receiver : receivers)
			game.updateCards(receiver, cards);
	}

	public MessagePlayerState toMessage(boolean hide) {
		List<Card> hand = this.hand;
		if (hide) {
			hand = new ArrayList<>();
			for (var card : this.hand) {
				hand.add(Cards.EMPTY_CARD_TYPE.create().setId(card.getId()));
			}
		}
		return new MessagePlayerState(id, deck.size(), hand, board, resources, maxResources, mulligan);
	}

	public boolean isGameOver() {
		return isGameOver;
	}

	public void setGameOver() {
		isGameOver = true;
	}
}
