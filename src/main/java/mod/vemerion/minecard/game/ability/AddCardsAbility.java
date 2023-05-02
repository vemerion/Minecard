package mod.vemerion.minecard.game.ability;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.LazyCardType;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;

public class AddCardsAbility extends CardAbility {

	public static final Codec<AddCardsAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger),
					LazyCardType.CODEC.fieldOf("card").forGetter(AddCardsAbility::getCard))
			.apply(instance, AddCardsAbility::new));

	private final LazyCardType toAdd;

	public AddCardsAbility(CardAbilityTrigger trigger, LazyCardType toAdd) {
		super(trigger);
		this.toAdd = toAdd;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.ADD_CARDS.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		return new Object[] { trigger.getText(), toAdd.get(true).getName() };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other) {
		state.addCards(receivers, List.of(toAdd.get(false).create()));
	}

	public LazyCardType getCard() {
		return toAdd;
	}

}