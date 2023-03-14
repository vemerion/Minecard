package mod.vemerion.minecard.game;

import java.util.List;
import java.util.UUID;

public class ClientPlayerState {

	public UUID id;
	public int deck;
	public List<Card> hand;
	public List<Card> board;

	public ClientPlayerState(UUID id, int deck, List<Card> hand, List<Card> board) {
		this.id = id;
		this.deck = deck;
		this.hand = hand;
		this.board = board;
	}
}
