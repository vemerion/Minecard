package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.util.ExtraCodecs;

public class SelectCardsAbility extends CardAbility {

	public static final Codec<SelectCardsAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(CardAbilitySelection.CODEC.fieldOf("selection").forGetter(SelectCardsAbility::getSelection),
							Codec.BOOL.optionalFieldOf("clear", false).forGetter(SelectCardsAbility::shouldClear))
					.apply(instance, SelectCardsAbility::new)));

	private final CardAbilitySelection selection;
	private final boolean clear;

	public SelectCardsAbility(CardAbilitySelection selection, boolean clear) {
		super(Set.of(), "");
		this.selection = selection;
		this.clear = clear;
	}

	public SelectCardsAbility(CardAbilitySelection selection) {
		this(selection, false);
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.SELECT_CARDS.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, Card cause, @Nullable Card target,
			Collected collected) {
		var selected = selection.select(receivers, state.getGame(), this, state.getId(), card, cause, target,
				collected);
		if (shouldClear()) {
			collected.get(0).clear();
		}
		collected.get(0).addAll(selected);
	}

	public CardAbilitySelection getSelection() {
		return selection;
	}

	public boolean shouldClear() {
		return clear;
	}

}
