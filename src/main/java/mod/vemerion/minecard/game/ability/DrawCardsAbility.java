package mod.vemerion.minecard.game.ability;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;

public class DrawCardsAbility extends CardAbility {

	public static final Codec<DrawCardsAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger),
					Codec.INT.fieldOf("count").forGetter(DrawCardsAbility::getCount))
			.apply(instance, DrawCardsAbility::new));

	private final int count;

	public DrawCardsAbility(CardAbilityTrigger trigger, int count) {
		super(trigger);
		this.count = count;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.DRAW_CARDS.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		return new Object[] { GameUtil.emphasize(trigger.getText()), count };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other) {
		state.drawCards(receivers, count);

		state.getGame().addHistory(receivers,
				new HistoryEntry(HistoryEntry.Type.ABILITY, state.getId(), card, List.of()));
	}

	public int getCount() {
		return count;
	}

}
