package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameState;
import mod.vemerion.minecard.game.GameUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum CardAbilityGroup {
	ALL("all"), SELF("self"), TARGET("target"), BOARD("board"), ENEMY_BOARD("enemy_board"), YOUR_BOARD("your_board"),
	HANDS("hands"), ENEMY_HAND("enemy_hand"), YOUR_HAND("your_hand"), ADJACENT("adjacent");

	public static final Codec<CardAbilityGroup> CODEC = GameUtil.enumCodec(CardAbilityGroup.class,
			CardAbilityGroup::getName);

	private String name;

	private CardAbilityGroup(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTextKey() {
		return "card_ability_group." + Main.MODID + "." + getName();
	}

	public Component getText() {
		return new TranslatableComponent(getTextKey());
	}

	public boolean singular() {
		return this == SELF || this == TARGET;
	}

	public List<Card> get(GameState state, UUID id, Card self, Card target) {
		List<Card> result = new ArrayList<>();

		var yourState = state.getYourPlayerState(id);
		var enemyState = state.getEnemyPlayerState(id);

		switch (this) {
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
		case ALL:
			for (var playerState : state.getPlayerStates()) {
				result.addAll(playerState.getBoard());
				result.addAll(playerState.getHand());
			}
			break;
		case BOARD:
			for (var playerState : state.getPlayerStates()) {
				result.addAll(playerState.getBoard());
			}
			break;
		case ENEMY_BOARD:
			result.addAll(enemyState.getBoard());
			break;
		case ENEMY_HAND:
			result.addAll(enemyState.getHand());
			break;
		case HANDS:
			for (var playerState : state.getPlayerStates()) {
				result.addAll(playerState.getHand());
			}
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
		}

		return result;
	}
}
