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

public class AddAbilityAbility extends CardAbility {

	public static final Codec<AddAbilityAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(CardAbility.CODEC.fieldOf("ability").forGetter(AddAbilityAbility::getAbility))
					.apply(instance, AddAbilityAbility::new)));

	private final CardAbility ability;

	public AddAbilityAbility(CardAbility ability) {
		super(Set.of(), "");
		this.ability = ability;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.ADD_ABILITY.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected) {
		for (var selected : collected.get(0)) {
			selected.setAbility(new MultiAbility("", List.of(ability, selected.getAbility())));
		}

		for (var receiver : receivers) {
			state.getGame().updateCards(receiver, collected.get(0));
		}
	}

	public CardAbility getAbility() {
		return ability;
	}

}
