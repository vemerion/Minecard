package mod.vemerion.minecard.game;

import java.util.List;

public class GameUtil {

	public static boolean canBeAttacked(Card card, List<? extends Card> board) {
		if (card.hasProperty(CardProperty.STEALTH))
			return false;

		return card.hasProperty(CardProperty.TAUNT) || board.stream()
				.noneMatch(c -> c.hasProperty(CardProperty.TAUNT) && !c.hasProperty(CardProperty.STEALTH));
	}
}
