package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameState {

	private List<PlayerState> playerStates;
	private int turn;

	public GameState() {
		playerStates = new ArrayList<>();
	}
	
	public List<PlayerState> getPlayerStates() {
		return playerStates;
	}

	public UUID getCurrentPlayer() {
		return playerStates.get(turn % 2).getId();
	}
}
