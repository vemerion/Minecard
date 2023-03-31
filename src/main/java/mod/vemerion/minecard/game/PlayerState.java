package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.network.DrawCardMessage;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.PlaceCardMessage;
import mod.vemerion.minecard.network.SetResourcesMessage;
import net.minecraft.core.SerializableUUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public class PlayerState {

	public static final Codec<PlayerState> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(SerializableUUID.CODEC.fieldOf("id").forGetter(PlayerState::getId),
					Codec.list(Card.CODEC).fieldOf("deck").forGetter(PlayerState::getDeck),
					Codec.list(Card.CODEC).fieldOf("hand").forGetter(PlayerState::getHand),
					Codec.list(Card.CODEC).fieldOf("board").forGetter(PlayerState::getBoard),
					Codec.INT.fieldOf("resources").forGetter(PlayerState::getResources),
					Codec.INT.fieldOf("maxResources").forGetter(PlayerState::getMaxResources))
			.apply(instance, PlayerState::new));

	private UUID id;
	private List<Card> deck;
	private List<Card> hand;
	private List<Card> board;
	private int resources;
	private int maxResources;

	public PlayerState(UUID id, List<Card> deck, List<Card> hand, List<Card> board, int resources, int maxResources) {
		this.id = id;
		this.deck = deck;
		this.hand = hand;
		this.board = board;
		this.resources = resources;
		this.maxResources = maxResources;
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

	public void newTurn(List<ServerPlayer> receivers) {
		maxResources = Math.min(10, maxResources + 1);
		resources = maxResources;
		for (var card : board)
			card.setReady(true);

		if (!deck.isEmpty()) {
			var card = deck.remove(deck.size() - 1);
			hand.add(card);
			for (var receiver : receivers) {
				Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
						new DrawCardMessage(id, receiver.getUUID().equals(id) ? card : Cards.EMPTY, true));
			}
		}
	}

	public void playCard(List<ServerPlayer> receivers, int cardIndex, int position) {
		if (cardIndex < 0 || position < 0 || hand.size() <= cardIndex || board.size() < position)
			return;

		var card = hand.get(cardIndex);
		if (card.getCost() > resources)
			return;

		resources -= card.getCost();
		board.add(position, card);
		hand.remove(cardIndex);

		for (var receiver : receivers) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
					new SetResourcesMessage(id, resources, maxResources));
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
					new PlaceCardMessage(id, card, cardIndex, position));
		}
	}

	public MessagePlayerState toMessage(boolean hide) {
		List<Card> hand = this.hand;
		if (hide) {
			hand = new ArrayList<>();
			for (int i = 0; i < this.hand.size(); i++) {
				hand.add(Cards.EMPTY);
			}
		}
		return new MessagePlayerState(id, deck.size(), hand, board, resources, maxResources);
	}
}
