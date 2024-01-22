package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameState;
import mod.vemerion.minecard.game.Receiver;
import net.minecraft.util.ExtraCodecs;

public record CardAbilitySelection(CardAbilityGroups groups, CardSelectionMethod method, CardCondition condition) {

	public static final Codec<CardAbilitySelection> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(CardAbilityGroups.CODEC.fieldOf("groups").forGetter(CardAbilitySelection::groups),
							CardSelectionMethod.CODEC.fieldOf("method").forGetter(CardAbilitySelection::method),
							CardCondition.CODEC.optionalFieldOf("condition", CardCondition.NoCondition.NO_CONDITION)
									.forGetter(CardAbilitySelection::condition))
					.apply(instance, CardAbilitySelection::new)));

	public List<Card> select(List<Receiver> receivers, GameState state, CardAbility ability, UUID id, Card self,
			Card cause, Card target, Collected collected) {
		List<Card> candidates = condition.filter(groups.get(state, id, self, cause, target, collected.get(0)),
				collected, state.getRandom());

		if (candidates.isEmpty())
			return candidates;

		return method.select(receivers, state, ability, candidates);
	}
}
