package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.UpdateCardMessage;
import net.minecraft.server.level.ServerPlayer;
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
		turn++;
		var current = getCurrentPlayerState();
		current.newTurn(receivers);
	}

	public void attack(List<ServerPlayer> receivers, int attacker, int target) {
		var current = getCurrentPlayerState();
		var enemy = getEnemyPlayerState();

		if (attacker < 0 || target < 0 || current.getBoard().size() <= attacker || enemy.getBoard().size() <= target
				|| !current.getBoard().get(attacker).isReady())
			return;

		var attackerCard = current.getBoard().get(attacker);
		var targetCard = enemy.getBoard().get(target);

		attackerCard.hurt(targetCard.getDamage());
		targetCard.hurt(attackerCard.getDamage());
		if (attackerCard.isDead())
			current.getBoard().remove(attacker);
		if (targetCard.isDead())
			enemy.getBoard().remove(target);

		for (var receiver : receivers) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
					new UpdateCardMessage(current.getId(), attackerCard, attacker));
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver),
					new UpdateCardMessage(enemy.getId(), targetCard, target));
		}
	}
}
