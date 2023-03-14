package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.SerializableUUID;

public class PlayerState {

	public static final Codec<PlayerState> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(SerializableUUID.CODEC.fieldOf("id").forGetter(PlayerState::getId),
					Codec.list(Card.CODEC).fieldOf("deck").forGetter(PlayerState::getDeck),
					Codec.list(Card.CODEC).fieldOf("hand").forGetter(PlayerState::getHand),
					Codec.list(Card.CODEC).fieldOf("board").forGetter(PlayerState::getBoard))
			.apply(instance, PlayerState::new));

	private UUID id;
	private List<Card> deck;
	private List<Card> hand;
	private List<Card> board;

	public PlayerState(UUID id, List<Card> deck, List<Card> hand, List<Card> board) {
		this.id = id;
		this.deck = deck;
		this.hand = hand;
		this.board = board;
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

	public ClientPlayerState toClient(boolean hide) {
		List<Card> hand = this.hand;
		if (hide) {
			hand = new ArrayList<>();
			for (int i = 0; i < this.hand.size(); i++) {
				hand.add(Cards.EMPTY);
			}
		}
		return new ClientPlayerState(id, deck.size(), hand, board);
	}
}
