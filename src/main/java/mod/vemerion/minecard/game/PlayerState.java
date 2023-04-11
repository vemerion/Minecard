package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.network.DrawCardsMessage;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.PlaceCardMessage;
import mod.vemerion.minecard.network.SetPropertiesMessage;
import mod.vemerion.minecard.network.SetResourcesMessage;
import net.minecraft.core.SerializableUUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraftforge.network.PacketDistributor;

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
	private GameState game;

	public PlayerState(UUID id, List<Card> deck, List<Card> hand, List<Card> board, int resources, int maxResources) {
		this.id = id;
		this.deck = deck;
		this.hand = hand;
		this.board = board;
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

	public void drawCards(List<ServerPlayer> receivers, int count) {
		List<Card> cards = new ArrayList<>();
		List<Card> fakes = new ArrayList<>();
		while (!deck.isEmpty() && count > 0) {
			var card = deck.remove(deck.size() - 1);
			hand.add(card);
			cards.add(card);
			fakes.add(Cards.EMPTY_CARD_TYPE.create().setId(card.getId()));
			count--;
		}

		for (var receiver : receivers) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
					new DrawCardsMessage(id, receiver.getUUID().equals(id) ? cards : fakes, true));
		}
	}

	public void addCards(List<ServerPlayer> receivers, List<Card> cards) {
		List<Card> fakes = new ArrayList<>();
		for (var card : cards)
			fakes.add(Cards.EMPTY_CARD_TYPE.create().setId(card.getId()));

		hand.addAll(cards);
		for (var receiver : receivers) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
					new DrawCardsMessage(id, receiver.getUUID().equals(id) ? cards : fakes, false));
		}
	}

	public void endTurn(List<ServerPlayer> receivers) {
		for (var card : board) {
			if (card.hasProperty(CardProperty.FREEZE)) {
				card.removeProperty(CardProperty.FREEZE);
				for (var receiver : receivers)
					Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
							new SetPropertiesMessage(id, card.getId(), card.getProperties()));
			}
		}
	}

	public void newTurn(List<ServerPlayer> receivers) {
		maxResources = Math.min(10, maxResources + 1);
		resources = maxResources;
		for (var card : board)
			if (!card.hasProperty(CardProperty.FREEZE))
				card.setReady(true);

		drawCards(receivers, 1);
	}

	public void addResources(List<ServerPlayer> receivers, int temporaryResources, int permanentResources) {
		resources = Mth.clamp(resources + temporaryResources, 0, 10);
		maxResources = Mth.clamp(maxResources + permanentResources, 0, 10);

		var msg = new SetResourcesMessage(id, resources, maxResources);

		for (var receiver : receivers)
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver), msg);
	}

	public void playCard(List<ServerPlayer> receivers, int cardId, int position) {
		var card = findFromHand(cardId);
		if (card == null || position < 0 || board.size() < position)
			return;

		if (card.getCost() > resources)
			return;

		if (card.hasProperty(CardProperty.CHARGE))
			card.setReady(true);

		resources -= card.getCost();

		if (!card.isSpell())
			board.add(position, card);

		hand.remove(card);

		for (var receiver : receivers) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
					new SetResourcesMessage(id, resources, maxResources));
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
					new PlaceCardMessage(id, card, position));
		}

		card.getAbility().onSummon(receivers, this, card);
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
