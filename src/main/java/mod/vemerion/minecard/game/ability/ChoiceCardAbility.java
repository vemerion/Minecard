package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardType;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;

public class ChoiceCardAbility extends CardAbility {

	public static final Codec<ChoiceCardAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder.create(instance -> instance
							.group(ExtraCodecs.nonEmptyList(Codec.list(CardAbility.CODEC)).fieldOf("abilities")
									.forGetter(ChoiceCardAbility::getAbilities))
							.apply(instance, ChoiceCardAbility::new)));

	private final List<CardAbility> abilities;

	public ChoiceCardAbility(List<CardAbility> abilities) {
		super(CardAbilityTrigger.SUMMON);
		this.abilities = abilities;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.CHOICE.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		var text = TextComponent.EMPTY.copy();
		for (var ability : abilities) {
			text.append("  ");
			text.append(ability.getDescription());
			text.append("\n");
		}
		return new Object[] {
				GameUtil.emphasize(
						new TranslatableComponent(ModCardAbilities.CHOICE.get().getTranslationKey() + ".choice_of")),
				text };
	}

	@Override
	public void createChoices(List<Receiver> receivers, PlayerState state, Card card) {
		var choices = state.getChoices();
		var selected = choices.getSelected(this);
		selected.ifPresentOrElse(c -> {
			abilities.get(c.getId()).createChoices(receivers, state, card);
		}, () -> {
			var cards = new ArrayList<Card>();
			for (int i = 0; i < abilities.size(); i++)
				cards.add(new CardType(card.getType().get(), 0, 0, 0, Map.of(), abilities.get(i), Map.of(),
						card.getAdditionalData(), CardType.DEFAULT_DECK_COUNT, CardType.DEFAULT_DROP_CHANCE).create()
						.setId(i));
			choices.addChoice(receivers, this, cards, false);
		});
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other) {
		state.getChoices().getSelected(this)
				.ifPresent(c -> abilities.get(c.getId()).invoke(receivers, state, card, other));
	}

	public List<CardAbility> getAbilities() {
		return abilities;
	}

}
