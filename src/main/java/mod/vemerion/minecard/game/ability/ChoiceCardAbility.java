package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardType;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.util.ExtraCodecs;

public class ChoiceCardAbility extends CardAbility {

	public static final Codec<ChoiceCardAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(Codec.STRING.fieldOf("text_key").forGetter(CardAbility::getTextKey),
							ExtraCodecs.nonEmptyList(Codec.list(CardAbility.CODEC)).fieldOf("abilities")
									.forGetter(ChoiceCardAbility::getAbilities))
					.apply(instance, ChoiceCardAbility::new)));

	private final List<CardAbility> abilities;

	public ChoiceCardAbility(String textKey, List<CardAbility> abilities) {
		super(Set.of(CardAbilityTrigger.SUMMON), textKey);
		this.abilities = abilities;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.CHOICE.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected) {
		var cards = new ArrayList<Card>();
		for (int i = 0; i < abilities.size(); i++)
			cards.add(new CardType(card.getType().get(), 0, 0, 0, Map.of(), abilities.get(i), card.getAdditionalData(),
					CardType.DEFAULT_DECK_COUNT, CardType.DEFAULT_DROP_CHANCE).create().setId(i));
		state.getGame().getChoice().make(receivers, this, cards, false, state.getGame().getRandom(), state.getId())
				.ifPresent(c -> {
					abilities.get(c.getId()).invoke(receivers, state, card, other, collected);
				});
		;
	}

	public List<CardAbility> getAbilities() {
		return abilities;
	}

}
