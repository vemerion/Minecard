package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameState;
import net.minecraft.util.ExtraCodecs;

public record CardAbilitySelection(CardAbilityGroup group, CardSelectionMethod method, CardCondition condition) {

	public static final Codec<CardAbilitySelection> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(CardAbilityGroup.CODEC.fieldOf("group").forGetter(CardAbilitySelection::group),
							CardSelectionMethod.CODEC.fieldOf("method").forGetter(CardAbilitySelection::method),
							CardCondition.CODEC.optionalFieldOf("condition", CardCondition.NoCondition.NO_CONDITION)
									.forGetter(CardAbilitySelection::condition))
					.apply(instance, CardAbilitySelection::new)));

	public List<Card> select(GameState state, UUID id, Card self, Card target) {
		List<Card> candidates = condition.filter(group.get(state, id, self, target));

		if (candidates.isEmpty())
			return candidates;

		switch (method) {
		case ALL:
			return candidates;
		case RANDOM:
			var result = new ArrayList<Card>();
			result.add(candidates.get(state.getRandom().nextInt(candidates.size())));
			return result;
		}

		return new ArrayList<>();
	}
}
