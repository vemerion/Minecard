package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.network.DrawCardsMessage;
import mod.vemerion.minecard.network.PlaceCardMessage;
import mod.vemerion.minecard.network.SetPropertiesMessage;
import mod.vemerion.minecard.network.SetResourcesMessage;
import net.minecraft.core.SerializableUUID;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;

public class PlayerState {

	public static final Codec<PlayerState> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(SerializableUUID.CODEC.fieldOf("id").forGetter(PlayerState::getId),
							Codec.list(Card.CODEC).fieldOf("deck").forGetter(PlayerState::getDeck),
							Codec.list(Card.CODEC).fieldOf("hand").forGetter(PlayerState::getHand),
							Codec.list(Card.CODEC).fieldOf("board").forGetter(PlayerState::getBoard),
							Codec.INT.fieldOf("resources").forGetter(PlayerState::getResources),
							Codec.INT.fieldOf("maxResources").forGetter(PlayerState::getMaxResources))
					.apply(instance, PlayerState::new)));

	private UUID id;
	private List<Card> deck;
	private List<Card> hand;
	private List<Card> board;
	private int resources;
	private int maxResources;
	private PlayerChoices choices;
	private GameState game;

	public PlayerState(UUID id, List<Card> deck, List<Card> hand, List<Card> board, int resources, int maxResources) {
		this.id = id;
		this.deck = new ArrayList<>(deck);
		this.hand = new ArrayList<>(hand);
		this.board = new ArrayList<>(board);
		this.resources = resources;
		this.maxResources = maxResources;
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

	public int getResources() {
		return resources;
	}

	public int getMaxResources() {
		return maxResources;
	}

	public PlayerChoices getChoices() {
		return choices;
	}

	public void resetChoices() {
		choices = null;
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

	public List<Integer> getReady() {
		var list = new ArrayList<Integer>();
		for (var card : board) {
			if (card.isReady())
				list.add(card.getId());
		}
		return list;
	}

	public void drawCards(List<Receiver> receivers, int count) {
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
		for (int i = board.size() - 1; i >= 0; i--) {
			var card = board.get(i);
			boolean changed = false;
			if (card.hasProperty(CardProperty.FREEZE)) {
				card.decrementProperty(CardProperty.FREEZE);
				changed = true;
			}
			if (card.hasProperty(CardProperty.BURN)) {
				game.hurt(receivers, card, 1);
				card.decrementProperty(CardProperty.BURN);
				changed = true;
			}
			if (card.hasProperty(CardProperty.POISON) && card.getHealth() > 1) {
				game.hurt(receivers, card, 1);
				changed = true;
			}
			if (changed && !card.isDead()) {
				var msg = new SetPropertiesMessage(id, card.getId(), card.getProperties());
				for (var receiver : receivers)
					receiver.receiver(msg);
			}

			card.getAbility().onTick(receivers, this, card);
		}
	}

	public void newTurn(List<Receiver> receivers) {
		maxResources = Math.min(10, maxResources + 1);
		resources = maxResources;

		List<Card> updated = new ArrayList<>();
		for (var card : board) {
			if (!card.hasProperty(CardProperty.FREEZE))
				card.setReady(true);

			if (card.hasProperty(CardProperty.BABY)) {
				card.decrementProperty(CardProperty.BABY);
				if (!card.hasProperty(CardProperty.BABY)) {
					card.getAbility().onGrow(receivers, this, card);
				}
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

	public void playCard(List<Receiver> receivers, int cardId, int leftId) {
		var card = findFromHand(cardId);
		var left = findFromBoard(leftId);
		if (card == null || (leftId != -1 && left == null))
			return;

		if (card.getCost() > resources || board.size() >= GameState.MAX_BOARD_SIZE)
			return;

		if (choices == null) {
			choices = new PlayerChoices(getId(), card, leftId);
		}

		card.getAbility().createChoices(receivers, this, card);

		if (choices.getPendingCount() != 0) {
			return;
		}

		if (card.hasProperty(CardProperty.CHARGE))
			card.setReady(true);

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

		card.getAbility().onSummon(receivers, this, card);

		choices = null;

		game.addHistory(receivers, new HistoryEntry(HistoryEntry.Type.PLAY_CARD, card, List.of()));
	}

	public MessagePlayerState toMessage(boolean hide) {
		List<Card> hand = this.hand;
		if (hide) {
			hand = new ArrayList<>();
			for (var card : this.hand) {
				hand.add(Cards.EMPTY_CARD_TYPE.create().setId(card.getId()));
			}
		}
		return new MessagePlayerState(id, deck.size(), hand, board, resources, maxResources);
	}
}
