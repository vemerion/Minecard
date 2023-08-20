package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import mod.vemerion.minecard.capability.PlayerStats;
import mod.vemerion.minecard.network.GameClient;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class AIPlayer implements GameClient {

	public static final UUID ID_1 = UUID.fromString("8dfb8e37-27f3-4ebd-870f-9a8e4e7a123d");
	public static final UUID ID_2 = UUID.fromString("57d76b03-0570-422e-93b9-10603073fce5");

	public static boolean isAi(UUID id) {
		return ID_1.equals(id) || ID_2.equals(id);
	}

	private List<Card> yourHand = new ArrayList<>();
	private List<Card> yourBoard = new ArrayList<>();
	private List<Card> enemyBoard = new ArrayList<>();
	private int resources;
	private boolean isGameOver;
	private boolean isCurrent;
	private boolean yourMulligan;
	private boolean enemyMulligan;
	private int timer;
	private GameBlockEntity game;
	private Random rand;
	private List<Choice> choices = new ArrayList<>();
	private UUID id;

	public AIPlayer(GameBlockEntity game, UUID id) {
		this.game = game;
		this.id = id;
		this.rand = new Random();
	}

	public UUID getId() {
		return id;
	}

	public void tick() {
		if (!game.playersPresent())
			return;

		timer++;

		if (yourMulligan && timer % 8 == 0) {
			game.performMulligan(getId(),
					yourHand.stream().filter(c -> c.getCost() > 3).map(c -> c.getId()).collect(Collectors.toSet()));
			return;
		}

		if (isGameOver || !isCurrent || timer % 8 != 0 || yourMulligan || enemyMulligan)
			return;

		if (game.isTutorial()) {
			game.endTurn();
			return;
		}

		if (!choices.isEmpty()) {
			var choice = choices.get(0);
			game.choice(choice.cards().get(rand.nextInt(choice.cards().size())).getId());
			choices.remove(0);
			return;
		}

		if (yourBoard.size() < GameState.MAX_BOARD_SIZE) {
			for (int i = 0; i < yourHand.size(); i++) {
				if (yourHand.get(i).getCost() <= resources) {
					game.playCard(yourHand.get(i).getId(), yourBoard.isEmpty() || rand.nextBoolean() ? -1
							: yourBoard.get(yourBoard.size() - 1).getId());
					return;
				}
			}
		}

		for (int i = 0; i < yourBoard.size(); i++) {
			var attacker = yourBoard.get(i);
			if (attacker.canAttack()) {
				var target = findTarget(attacker);
				if (target != null) {
					game.attack(attacker.getId(), target.getId());
					return;
				}
			}
		}

		game.endTurn();
	}

	private Card findTarget(Card attacker) {
		var totalDamage = yourBoard.stream().mapToInt(c -> c.canAttack() ? c.getDamage() : 0).sum();
		var enemyPlayer = enemyBoard.stream().dropWhile(c -> c.getType().orElse(null) != EntityType.PLAYER).findFirst()
				.get();

		// Attack player if it leads to win or with small random chance
		if (GameUtil.canBeAttacked(enemyPlayer, enemyBoard)
				&& (totalDamage >= enemyPlayer.getHealth() || rand.nextDouble() < 0.1)) {
			return enemyPlayer;
		}

		var candidates = enemyBoard.stream()
				.filter(c -> c.getType().orElse(null) != EntityType.PLAYER && GameUtil.canBeAttacked(c, enemyBoard))
				.collect(Collectors.toCollection(() -> new ArrayList<>()));
		Collections.shuffle(candidates);

		// Can kill without dying
		for (var card : candidates) {
			if (card.getDamage() < attacker.getHealth() && card.getHealth() <= attacker.getDamage()) {
				return card;
			}
		}

		// Can kill but dies
		for (var card : candidates) {
			if (card.getHealth() <= attacker.getDamage()) {
				return card;
			}
		}

		// Can't kill but does not die
		for (var card : candidates) {
			if (card.getDamage() < attacker.getHealth()) {
				return card;
			}
		}

		if (GameUtil.canBeAttacked(enemyPlayer, enemyBoard))
			candidates.add(enemyPlayer);

		// A random target
		if (!candidates.isEmpty())
			return candidates.get(rand.nextInt(candidates.size()));

		return null;
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
	public void setProperties(UUID id, int cardId, Map<ResourceLocation, Integer> properties) {
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
		if (id.equals(getId())) {
			for (var card : cards)
				yourHand.add(card);
		}
	}

	@Override
	public void updateCard(Card received) {
		var card = find(received.getId());
		if (card != null) {
			card.copy(received);
			if (card.isDead())
				List.of(yourHand, yourBoard, enemyBoard).forEach(l -> l.remove(card));
		}
	}

	@Override
	public void placeCard(UUID id, Card card, int leftId) {
		var board = id.equals(getId()) ? yourBoard : enemyBoard;
		if (id.equals(getId())) {
			yourHand.removeIf(c -> c.getId() == card.getId());
		}

		if (!card.isSpell()) {
			if (leftId == -1) {
				board.add(0, card);
			}

			for (int i = 0; i < board.size(); i++) {
				if (board.get(i).getId() == leftId) {
					board.add(i + 1, card);
				}
			}
		}
	}

	@Override
	public void setResources(UUID id, int resources, int maxResources) {
		if (getId().equals(id))
			this.resources = resources;
	}

	@Override
	public void setCurrent(UUID current) {
		isCurrent = getId().equals(current);
	}

	@Override
	public void openGame(List<MessagePlayerState> state, BlockPos pos) {
		for (var playerState : state) {
			if (playerState.id.equals(getId())) {
				yourHand = playerState.hand;
				yourBoard = playerState.board;
				yourMulligan = playerState.mulligan;
			} else {
				enemyBoard = playerState.board;
				enemyMulligan = playerState.mulligan;
			}
		}
	}

	@Override
	public void playerChoice(Choice choice) {
		choices.add(choice);
	}

	@Override
	public void history(HistoryEntry entry) {

	}

	@Override
	public void mulliganDone(UUID id) {
		if (id.equals(getId())) {
			yourMulligan = false;
		} else {
			enemyMulligan = false;
		}
	}

	@Override
	public void stat(PlayerStats.Key key, int value, String name) {

	}
}
