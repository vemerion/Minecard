package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class CardAbilityGroups {

	public static final List<Pair<CardAbilityGroup, CardAbilityGroup>> INCOMPATIBLE = List.of(
			Pair.of(CardAbilityGroup.ADJACENT, CardAbilityGroup.YOUR_BOARD),
			Pair.of(CardAbilityGroup.SELF, CardAbilityGroup.YOUR_BOARD),
			Pair.of(CardAbilityGroup.TARGET, CardAbilityGroup.ENEMY_BOARD),
			Pair.of(CardAbilityGroup.TARGET_ADJACENT, CardAbilityGroup.ENEMY_BOARD));

	public static final Codec<CardAbilityGroups> CODEC = Codec.list(CardAbilityGroup.CODEC).comapFlatMap(list -> {
		Set<CardAbilityGroup> groups = EnumSet.copyOf(list);

		if (groups.isEmpty()) {
			return DataResult.error("Card group list cannot be empty");
		}

		if (groups.size() != list.size()) {
			return DataResult.error("Card group list has duplicate entries");
		}

		if (groups.contains(CardAbilityGroup.ALL) && groups.size() != 1) {
			return DataResult.error("Card group 'all' is incompatible with all other groups");
		}

		for (var pair : INCOMPATIBLE) {
			if (groups.contains(pair.getLeft()) && groups.contains(pair.getRight()))
				return DataResult
						.error("Card groups '" + pair.getLeft() + "' is incompatible with '" + pair.getRight() + "'");
		}

		return DataResult.success(new CardAbilityGroups(groups));
	}, a -> List.copyOf(a.groups));

	private final Set<CardAbilityGroup> groups;

	public CardAbilityGroups(Set<CardAbilityGroup> groups) {
		this.groups = groups;
	}

	public Component getText() {
		var result = TextComponent.EMPTY.copy();
		int i = 0;
		for (var group : groups) {
			result.append(group.getText());
			if (i != groups.size() - 1) {
				result.append("/");
			}
			i++;
		}
		return result;
	}

	public boolean singular() {
		return groups.size() == 1 && groups.stream().allMatch(g -> g.singular());
	}

	public List<Card> get(GameState state, UUID id, Card self, Card target) {
		List<Card> result = new ArrayList<>();

		var yourState = state.getYourPlayerState(id);
		var enemyState = state.getEnemyPlayerState(id);

		for (var group : groups) {
			switch (group) {
			case ADJACENT: {
				var board = yourState.getBoard();
				for (int i = 0; i < board.size(); i++) {
					if (board.get(i) == self) {
						if (i != 0)
							result.add(board.get(i - 1));
						if (i != board.size() - 1)
							result.add(board.get(i + 1));
					}
				}
				break;
			}
			case TARGET_ADJACENT: {
				if (target != null) {
					var board = enemyState.getBoard();
					for (int i = 0; i < board.size(); i++) {
						if (board.get(i) == target) {
							if (i != 0)
								result.add(board.get(i - 1));
							if (i != board.size() - 1)
								result.add(board.get(i + 1));
						}
					}
				}
				break;
			}
			case ALL:
				for (var playerState : state.getPlayerStates()) {
					result.addAll(playerState.getDeck());
					result.addAll(playerState.getBoard());
					result.addAll(playerState.getHand());
				}
				break;
			case ENEMY_BOARD:
				result.addAll(enemyState.getBoard());
				break;
			case ENEMY_HAND:
				result.addAll(enemyState.getHand());
				break;
			case SELF:
				result.add(self);
				break;
			case TARGET:
				if (target != null)
					result.add(target);
				break;
			case YOUR_BOARD:
				result.addAll(yourState.getBoard());
				break;
			case YOUR_HAND:
				result.addAll(yourState.getHand());
				break;
			case ENEMY_DECK:
				result.addAll(enemyState.getDeck());
				break;
			case YOUR_DECK:
				result.addAll(yourState.getDeck());
				break;
			}
		}

		return result;
	}
}
