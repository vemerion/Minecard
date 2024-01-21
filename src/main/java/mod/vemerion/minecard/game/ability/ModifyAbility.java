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

public class ModifyAbility extends CardAbility {

	public static final Codec<ModifyAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder
							.create(instance -> instance
									.group(ExtraCodecs
											.nonEmptyList(Codec
													.list(ExtraCodecs.nonEmptyList(Codec.list(CardModification.CODEC))))
											.fieldOf("modifications").forGetter(ModifyAbility::getModifications))
									.apply(instance, ModifyAbility::new)));

	private final List<List<CardModification>> modifications;

	public ModifyAbility(List<List<CardModification>> modifications) {
		super(Set.of(), "");
		this.modifications = modifications;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.MODIFY.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, Card cause, @Nullable Card target,
			Collected collected) {
		var modification = modifications.get(state.getGame().getRandom().nextInt(modifications.size()));

		for (var selected : collected.get(0)) {
			for (var m : modification)
				m.getOutput().set(state, selected, receivers,
						m.getOperator().evaluate(state.getGame().getRandom(), selected, collected));
		}

		for (var receiver : receivers) {
			state.getGame().updateCards(receiver, collected.get(0));
		}
	}

	public List<List<CardModification>> getModifications() {
		return modifications;
	}

}
