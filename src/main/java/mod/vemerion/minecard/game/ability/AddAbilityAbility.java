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

	public static final Codec<AddAbilityAbility> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder
			.create(instance -> instance.group(Codec.INT.fieldOf("index").forGetter(AddAbilityAbility::getIndex))
					.apply(instance, AddAbilityAbility::new)));

	private final int index;

	public AddAbilityAbility(int index) {
		super(Set.of(), "");
		this.index = index;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.ADD_ABILITY.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected) {
		var coll = collected.get(index);
		var ability = coll.isEmpty() ? null : coll.get(state.getGame().getRandom().nextInt(coll.size())).getAbility();
		if (ability == null)
			return;

		for (var selected : collected.get(0)) {
			selected.setAbility(new MultiAbility("", List.of(ability, selected.getAbility())));
		}

		for (var receiver : receivers) {
			state.getGame().updateCards(receiver, collected.get(0));
		}
	}

	public int getIndex() {
		return index;
	}

}
