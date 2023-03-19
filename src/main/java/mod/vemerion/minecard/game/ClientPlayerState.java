package mod.vemerion.minecard.game;

import java.util.List;
import java.util.UUID;

public class ClientPlayerState {

	public UUID id;
	public int deck;
	public List<Card> hand;
	public List<Card> board;
	public int resources;
	public int maxResources;

	public ClientPlayerState(UUID id, int deck, List<Card> hand, List<Card> board, int resources, int maxResources) {
		this.id = id;
		this.deck = deck;
		this.hand = hand;
		this.board = board;
		this.resources = resources;
		this.maxResources = maxResources;
	}
}
