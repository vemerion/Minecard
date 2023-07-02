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
import net.minecraft.world.item.ItemStack;

public class SelectCardsAbility extends CardAbility {

	public static final Codec<SelectCardsAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(CardAbilitySelection.CODEC.fieldOf("selection").forGetter(SelectCardsAbility::getSelection))
					.apply(instance, SelectCardsAbility::new)));

	private final CardAbilitySelection selection;

	public SelectCardsAbility(CardAbilitySelection selection) {
		super(Set.of());
		this.selection = selection;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.SELECT_CARDS.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		return new Object[] { selection.getText() };
	}

	@Override
	public void createChoices(List<Receiver> receivers, PlayerState state, Card card) {
		selection.createChoice(receivers, this, state, card);
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			List<Card> collected, ItemStack icon) {
		var selected = selection.select(state.getGame(), this, state.getId(), card, other);
		collected.addAll(selected);
	}

	public CardAbilitySelection getSelection() {
		return selection;
	}

}
