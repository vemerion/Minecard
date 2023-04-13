package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import mod.vemerion.minecard.network.CombatMessage;
import mod.vemerion.minecard.network.Network;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.network.PacketDistributor;

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

	public void endTurn(List<ServerPlayer> receivers) {
		getCurrentPlayerState().endTurn(receivers);
		turn++;
		getCurrentPlayerState().newTurn(receivers);
	}

	public void attack(List<ServerPlayer> receivers, int attackerId, int targetId) {
		var current = getCurrentPlayerState();
		var enemy = getEnemyPlayerState();

		var attackerCard = current.findFromBoard(attackerId);
		var targetCard = enemy.findFromBoard(targetId);
		if (attackerCard == null || targetCard == null || !attackerCard.isReady())
			return;

		// Can't be targeted
		if (!GameUtil.canBeAttacked(targetCard, enemy.getBoard())) {
			return;
		}

		attackerCard.getAbility().onAttack(receivers, current, attackerCard, targetCard);

		attackerCard.setReady(false);
		attackerCard.removeProperty(CardProperty.STEALTH);
		current.hurt(receivers, attackerCard, targetCard.getDamage());
		enemy.hurt(receivers, targetCard, attackerCard.getDamage());

		for (var receiver : receivers) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
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
