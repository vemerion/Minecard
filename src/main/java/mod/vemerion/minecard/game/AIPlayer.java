package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import mod.vemerion.minecard.network.GameClient;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class AIPlayer implements GameClient {

	public static final UUID ID = UUID.fromString("8dfb8e37-27f3-4ebd-870f-9a8e4e7a123d");

	private List<Card> yourHand = new ArrayList<>();
	private List<Card> yourBoard = new ArrayList<>();
	private List<Card> enemyBoard = new ArrayList<>();
	private int resources;
	private boolean isGameOver;
	private boolean isCurrent;
	private int timer;
	private GameBlockEntity game;
	
	public AIPlayer(GameBlockEntity game) {
		this.game = game;
	}

	public void tick() {
		timer++;
		if (isGameOver || !isCurrent || timer % 8 != 0)
			return;

		for (int i = 0; i < yourHand.size(); i++) {
			if (yourHand.get(i).getCost() <= resources) {
				game.playCard(yourHand.get(i).getId(), -1);
				return;
			}
		}

		for (int i = 0; i < yourBoard.size(); i++) {
			var attacker = yourBoard.get(i);
			if (attacker.isReady()) {
				for (int j = 0; j < enemyBoard.size(); j++) {
					var target = enemyBoard.get(j);
					if (GameUtil.canBeAttacked(target, enemyBoard)) {
						game.attack(attacker.getId(), target.getId());
						return;
					}
				}
			}
		}

		game.endTurn();
	}

	private Card find(int id, List<Card> list) {
		for (var card : list)
			if (card.getId() == id)
				return card;
		return null;
	}

	private Card find(int id) {
		for (var list : List.of(yourHand, yourBoard, enemyBoard)) {
			var card = find(id, list);
			if (card != null)
				return card;
		}
		return null;
	}

	@Override
	public void animation(int originId, List<Integer> targets, ResourceLocation rl) {
	}

	@Override
	public void updateDecks(Map<UUID, Integer> sizes) {
	}

	@Override
	public void combat(UUID attackerId, int attackerCardId, UUID targetId, int targetCardId) {
	}

	@Override
	public void setProperties(UUID id, int cardId, Map<CardProperty, Integer> properties) {
		var card = find(cardId);
		if (card != null) {
			card.getProperties().clear();
			card.getProperties().putAll(properties);
		}
	}

	@Override
	public void gameOver() {
		isGameOver = true;
	}

	@Override
	public void drawCards(UUID id, List<Card> cards, boolean shrinkDeck) {
		if (id.equals(ID)) {
			for (var card : cards)
				yourHand.add(card);
		}
	}

	@Override
	public void updateCard(Card received) {
		var card = find(received.getId());
		if (card != null)
			card.copy(received);
	}

	@Override
	public void setReady(UUID id, List<Integer> cards) {
		for (var cardId : cards) {
			var card = find(cardId);
			if (card != null)
				card.setReady(true);
		}
	}

	@Override
	public void placeCard(UUID id, Card card, int leftId) {
		var board = id.equals(ID) ? yourBoard : enemyBoard;
		if (id.equals(ID)) {
			yourHand.removeIf(c -> c.getId() == card.getId());
		}

		if (leftId == -1) {
			board.add(0, card);
		}

		for (int i = 0; i < board.size(); i++) {
			if (board.get(i).getId() == leftId) {
				board.add(i + 1, card);
			}
		}
	}

	@Override
	public void setResources(UUID id, int resources, int maxResources) {
		if (ID.equals(id))
			this.resources = resources;
	}

	@Override
	public void setCurrent(UUID current) {
		isCurrent = ID.equals(current);
	}

	@Override
	public void openGame(List<MessagePlayerState> state, BlockPos pos) {
		for (var playerState : state) {
			if (playerState.id.equals(ID)) {
				yourHand = playerState.hand;
				yourBoard = playerState.board;
			} else {
				enemyBoard = playerState.board;
			}
		}
	}

}
