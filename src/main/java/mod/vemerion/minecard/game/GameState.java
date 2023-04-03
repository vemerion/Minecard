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

	public void attack(List<ServerPlayer> receivers, int attacker, int target) {
		var current = getCurrentPlayerState();
		var enemy = getEnemyPlayerState();

		if (attacker < 0 || target < 0 || current.getBoard().size() <= attacker || enemy.getBoard().size() <= target
				|| !current.getBoard().get(attacker).isReady())
			return;

		var attackerCard = current.getBoard().get(attacker);
		var targetCard = enemy.getBoard().get(target);

		// Can't be targeted
		if (targetCard.hasProperty(CardProperty.STEALTH)
				|| (!targetCard.hasProperty(CardProperty.TAUNT) && enemy.getBoard().stream()
						.anyMatch(c -> c.hasProperty(CardProperty.TAUNT) && !c.hasProperty(CardProperty.STEALTH)))) {
			return;
		}

		attackerCard.hurt(targetCard.getDamage());
		targetCard.hurt(attackerCard.getDamage());
		attackerCard.setReady(false);
		attackerCard.removeProperty(CardProperty.STEALTH);
		if (attackerCard.isDead())
			current.getBoard().remove(attacker);
		if (targetCard.isDead())
			enemy.getBoard().remove(target);

		for (var receiver : receivers) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
					new CombatMessage(current.getId(), attackerCard, attacker, enemy.getId(), targetCard, target));
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
