package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mod.vemerion.minecard.network.CombatMessage;
import mod.vemerion.minecard.network.Network;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.network.PacketDistributor;

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

		attackerCard.hurt(targetCard.getDamage());
		targetCard.hurt(attackerCard.getDamage());
		attackerCard.setReady(false);
		attackerCard.removeProperty(CardProperty.STEALTH);

		attackerCard.getAbility().onAttack(receivers, current, attackerCard, targetCard);

		if (attackerCard.isDead())
			current.getBoard().remove(attackerCard);
		if (targetCard.isDead())
			enemy.getBoard().remove(targetCard);

		for (var receiver : receivers) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
					new CombatMessage(current.getId(), attackerCard, enemy.getId(), targetCard));
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
