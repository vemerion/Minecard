package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.util.ExtraCodecs;

public class ConditionalAbility extends CardAbility {

	public static final Codec<ConditionalAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder.create(instance -> instance
							.group(Codec.STRING.fieldOf("text_key").forGetter(CardAbility::getTextKey),
									Codec.INT.optionalFieldOf("index").forGetter(ConditionalAbility::getIndex),
									CardCondition.CODEC.fieldOf("condition")
											.forGetter(ConditionalAbility::getCondition),
									CardAbility.CODEC.fieldOf("ability").forGetter(ConditionalAbility::getAbility))
							.apply(instance, ConditionalAbility::new)));

	private final Optional<Integer> index;
	private final CardCondition condition;
	private final CardAbility ability;

	public ConditionalAbility(String textKey, Optional<Integer> index, CardCondition condition, CardAbility ability) {
		super(ability.getTriggers(), textKey);
		this.index = index;
		this.condition = condition;
		this.ability = ability;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.CONDITIONAL.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, Card cause, @Nullable Card target,
			Collected collected) {
		index.ifPresentOrElse(i -> {
			for (var c : collected.get(i)) {
				if (!condition.test(c, collected, state.getGame().getRandom())) {
					return;
				}
			}
			ability.invoke(receivers, state, card, cause, target, collected);
		}, () -> {
			if (condition.test(card, collected, state.getGame().getRandom())) {
				ability.invoke(receivers, state, card, cause, target, collected);
			}
		});
	}

	public Optional<Integer> getIndex() {
		return index;
	}

	public CardCondition getCondition() {
		return condition;
	}

	public CardAbility getAbility() {
		return ability;
	}

}
