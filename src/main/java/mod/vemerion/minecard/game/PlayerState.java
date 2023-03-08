package mod.vemerion.minecard.game;

import java.util.List;

public class PlayerState {
	private List<Card> deck;
	private List<Card> hand;
	private List<Card> board;

	public PlayerState(List<Card> deck, List<Card> hand, List<Card> board) {
		this.deck = deck;
		this.hand = hand;
		this.board = board;
	}
}
