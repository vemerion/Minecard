package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.helper.Helper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
			Card target, Collected collected) {
		List<Card> candidates = condition.filter(groups.get(state, id, self, target, collected.get(0)), collected);

		if (candidates.isEmpty())
			return candidates;

		return method.select(receivers, state, ability, candidates);
	}

	public Component getText() {
		var EMPTY = TextComponent.EMPTY;
		return new TranslatableComponent(Helper.gui("card_ability_selection"),
				!groups.singular() ? method.getDescription() : EMPTY, groups.getText(),
				condition.isEmpty() ? EMPTY
						: (groups.singular() ? new TranslatableComponent(Helper.gui("if"))
								: new TranslatableComponent(Helper.gui("where"))),
				condition.getDescription());
	}
}
