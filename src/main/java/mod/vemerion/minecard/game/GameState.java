package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import mod.vemerion.minecard.network.CombatMessage;
import mod.vemerion.minecard.network.PlaceCardMessage;
import mod.vemerion.minecard.network.UpdateCardsMessage;
import mod.vemerion.minecard.network.UpdateDecksMessage;
import net.minecraft.world.entity.EntityType;

public class GameState {
	private List<PlayerState> playerStates;
	private int turn;
	private Random random;

	public GameState() {
		playerStates = new ArrayList<>();
		random = new Random();
	}

	public Random getRandom() {
		return random;
	}

	public List<PlayerState> getPlayerStates() {
		return playerStates;
	}

	public PlayerState getYourPlayerState(UUID id) {
		for (var playerState : playerStates)
			if (playerState.getId().equals(id))
				return playerState;
		return null;
	}

	public PlayerState getEnemyPlayerState(UUID id) {
		for (var playerState : playerStates)
			if (!playerState.getId().equals(id))
				return playerState;
		return null;
	}

	public UUID getCurrentPlayer() {
		return getCurrentPlayerState().getId();
	}

	public PlayerState getCurrentPlayerState() {
		return playerStates.get(turn % 2);
	}

	private PlayerState getEnemyPlayerState() {
		return playerStates.get((turn + 1) % 2);
	}

	public CardVisibility calcVisibility(UUID playerId, Card card) {
		for (var playerState : getPlayerStates()) {
			for (var c : playerState.getDeck()) {
				if (c.getId() == card.getId())
					return CardVisibility.DECK;
			}
			for (var c : playerState.getHand()) {
				if (c.getId() == card.getId())
					return playerId.equals(playerState.getId()) ? CardVisibility.VISIBLE : CardVisibility.ENEMY_HAND;
			}
			for (var c : playerState.getBoard())
				if (c.getId() == card.getId())
					return CardVisibility.VISIBLE;
		}
		return CardVisibility.UNKNOWN;
	}

	public void updateCards(Receiver receiver, List<Card> cards) {
		boolean updateDeckSizes = false;
		List<Card> updated = new ArrayList<>();
		for (var card : cards) {
			var visibility = calcVisibility(receiver.getId(), card);
			switch (visibility) {
			case DECK:
				updateDeckSizes = true;
				break;
			case ENEMY_HAND:
				if (card.isDead()) {
					updated.add(Cards.EMPTY_CARD_TYPE.create().setId(card.getId()));
				}
				break;
			case UNKNOWN:
				break;
			case VISIBLE:
				updated.add(card);
				break;

			}
		}

		receiver.receiver(new UpdateCardsMessage(updated));

		if (updateDeckSizes) {
			receiver.receiver(new UpdateDecksMessage(playerStates.stream()
					.map(s -> Pair.of(s.getId(), (int) s.getDeck().stream().filter(c -> !c.isDead()).count()))
					.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()))));
		}
	}

	public void hurt(List<Receiver> receivers, Card card, int amount) {
		if (card.isDead())
			return;

		card.hurt(amount);

		PlayerState owner = null;
		List<Card> container = null;

		for (var playerState : getPlayerStates()) {
			for (var list : List.of(playerState.getBoard(), playerState.getHand(), playerState.getDeck())) {
				if (list.stream().anyMatch(c -> c.getId() == card.getId())) {
					owner = playerState;
					container = list;
					break;
				}
			}
		}

		if (owner == null || container == null)
			return;

		card.getAbility().onHurt(receivers, owner, card);

		for (var receiver : receivers) {
			updateCards(receiver, List.of(card));
		}

		if (card.isDead()) {
			card.getAbility().onDeath(receivers, owner, card);
			container.remove(card);
		}
	}

	public void heal(List<Receiver> receivers, Card card, int amount) {
		var healthBefore = card.getHealth();
		card.setHealth(Math.min(card.getMaxHealth(), card.getHealth() + amount));

		if (healthBefore != card.getHealth()) {
			for (var receiver : receivers) {
				updateCards(receiver, List.of(card));
			}
		}
	}

	public void summonCard(List<Receiver> receivers, Card card, UUID id, int leftId) {
		if (card.hasProperty(CardProperty.CHARGE))
			card.setReady(true);

		var playerState = getYourPlayerState(id);
		var board = playerState.getBoard();

		if (leftId == -1) {
			board.add(0, card);
		} else {
			for (var i = 0; i < board.size(); i++) {
				if (board.get(i).getId() == leftId) {
					board.add(i + 1, card);
					break;
				}
			}
		}

		for (var receiver : receivers) {
			receiver.receiver(new PlaceCardMessage(id, card, leftId));
		}

		card.getAbility().onSummon(receivers, playerState, card);
	}

	public void endTurn(List<Receiver> receivers) {
		getCurrentPlayerState().endTurn(receivers);
		turn++;
		getCurrentPlayerState().newTurn(receivers);
	}

	public void attack(List<Receiver> receivers, int attackerId, int targetId) {
		var current = getCurrentPlayerState();
		var enemy = getEnemyPlayerState();

		var attackerCard = current.findFromBoard(attackerId);
		var targetCard = enemy.findFromBoard(targetId);
		if (attackerCard == null || targetCard == null || !attackerCard.canAttack())
			return;

		// Can't be targeted
		if (!GameUtil.canBeAttacked(targetCard, enemy.getBoard())) {
			return;
		}

		attackerCard.getAbility().onAttack(receivers, current, attackerCard, targetCard);

		attackerCard.setReady(false);
		attackerCard.removeProperty(CardProperty.STEALTH);
		hurt(receivers, attackerCard, targetCard.getDamage());
		hurt(receivers, targetCard, attackerCard.getDamage());

		for (var receiver : receivers) {
			receiver.receiver(
					new CombatMessage(current.getId(), attackerCard.getId(), enemy.getId(), targetCard.getId()));
		}
	}

	public boolean isGameOver() {
		int count = 0;
		for (var playerState : playerStates) {
			for (var card : playerState.getBoard()) {
				if (card.getType() == EntityType.PLAYER && !card.isDead()) {
					count++;
					break;
				}
			}
		}
		return count < playerStates.size();
	}
}
