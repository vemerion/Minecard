package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.util.ExtraCodecs;

public class ChainAbility extends CardAbility {

	public static final Codec<ChainAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder.create(instance -> instance
							.group(GameUtil.TRIGGERS_CODEC.fieldOf("triggers").forGetter(CardAbility::getTriggers),
									Codec.STRING.fieldOf("text_key").forGetter(CardAbility::getTextKey),
									ExtraCodecs.nonEmptyList(Codec.list(CardAbility.CODEC)).fieldOf("abilities")
											.forGetter(ChainAbility::getAbilities))
							.apply(instance, ChainAbility::new)));

	private final List<CardAbility> abilities;

	public ChainAbility(Set<CardAbilityTrigger> triggers, String textKey, List<CardAbility> abilities) {
		super(triggers, textKey);
		this.abilities = abilities;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.CHAIN.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected) {
		for (var ability : abilities)
			ability.invoke(receivers, state, card, other, collected);
	}

	@Override
	public void trigger(CardAbilityTrigger trigger, List<Receiver> receivers, PlayerState state, Card card,
			Card target) {
		if (triggers.contains(trigger)) {
			invoke(receivers, state, card, target, new Collected());
		}
	}

	public List<CardAbility> getAbilities() {
		return abilities;
	}

}
