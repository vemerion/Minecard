package mod.vemerion.minecard.screen;

import java.util.List;
import java.util.UUID;

public class ClientPlayerState {

	public UUID id;
	public int deck;
	public List<ClientCard> hand;
	public List<ClientCard> board;
	public int resources;
	public int maxResources;

	public ClientPlayerState(UUID id, int deck, List<ClientCard> hand, List<ClientCard> board, int resources,
			int maxResources) {
		this.id = id;
		this.deck = deck;
		this.hand = hand;
		this.board = board;
		this.resources = resources;
		this.maxResources = maxResources;
	}
}