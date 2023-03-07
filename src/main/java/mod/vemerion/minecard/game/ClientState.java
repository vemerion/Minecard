package mod.vemerion.minecard.game;

import java.util.List;

public class ClientState {

	public int enemyDeck;
	public int yourDeck;
	public int enemyHand;
	public List<Card> yourHand;
	public List<Card> enemyBoard;
	public List<Card> yourBoard;

	public ClientState(int enemyDeck, int yourDeck, int enemyHand, List<Card> yourHand, List<Card> enemyBoard,
			List<Card> yourBoard) {
		this.enemyDeck = enemyDeck;
		this.yourDeck = yourDeck;
		this.enemyHand = enemyHand;
		this.yourHand = yourHand;
		this.enemyBoard = enemyBoard;
		this.yourBoard = yourBoard;
	}

	
}
