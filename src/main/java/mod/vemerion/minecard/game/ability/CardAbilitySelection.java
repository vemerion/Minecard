package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameState;
import mod.vemerion.minecard.game.PlayerState;
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

	public void createChoice(List<Receiver> receivers, CardAbility ability, PlayerState state, Card card) {
		if (method == CardSelectionMethod.CHOICE) {
			var candidates = condition.filter(groups.get(state.getGame(), state.getId(), card, null));
			if (!candidates.isEmpty())
				state.getChoices().addChoice(receivers, ability, candidates);
		}
	}

	public List<Card> select(GameState state, CardAbility ability, UUID id, Card self, Card target) {
		List<Card> candidates = condition.filter(groups.get(state, id, self, target));

		if (candidates.isEmpty())
			return candidates;

		switch (method) {
		case ALL:
			return candidates;
		case RANDOM: {
			var result = new ArrayList<Card>();
			result.add(candidates.get(state.getRandom().nextInt(candidates.size())));
			return result;
		}
		case CHOICE: {
			var result = new ArrayList<Card>();
			var choices = state.getCurrentPlayerState().getChoices();
			if (choices == null) {
				Main.LOGGER.debug(
						"No choices made (can only make choice is ability trigger is 'summon'). Will pick random card");
				result.add(candidates.get(state.getRandom().nextInt(candidates.size())));
			} else {
				choices.getSelected(ability).ifPresent(c -> result.add(c));
			}
			return result;
		}
		}

		return new ArrayList<>();
	}

	public Component getText() {
		var EMPTY = TextComponent.EMPTY;
		return new TranslatableComponent(Helper.gui("card_ability_selection"),
				!groups.singular() ? method.getText() : EMPTY, groups.getText(),
				condition.isEmpty() ? EMPTY
						: (groups.singular() ? new TranslatableComponent(Helper.gui("if"))
								: new TranslatableComponent(Helper.gui("where"))),
				condition.getDescription());
	}
}
