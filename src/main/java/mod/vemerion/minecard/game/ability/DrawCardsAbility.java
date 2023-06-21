package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.world.item.ItemStack;

public class DrawCardsAbility extends CardAbility {

	public static final Codec<DrawCardsAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(GameUtil.TRIGGERS_CODEC.fieldOf("triggers").forGetter(CardAbility::getTriggers),
					Codec.INT.fieldOf("count").forGetter(DrawCardsAbility::getCount))
			.apply(instance, DrawCardsAbility::new));

	private final int count;

	public DrawCardsAbility(Set<CardAbilityTrigger> triggers, int count) {
		super(triggers);
		this.count = count;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.DRAW_CARDS.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		return new Object[] { GameUtil.emphasize(GameUtil.triggersToText(triggers)), count };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			ItemStack icon) {
		state.drawCards(receivers, count);

		state.getGame().addHistory(receivers, new HistoryEntry(icon, state.getId(), card, List.of()));
	}

	public int getCount() {
		return count;
	}

}
