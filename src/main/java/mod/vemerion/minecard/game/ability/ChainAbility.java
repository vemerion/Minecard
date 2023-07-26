package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class ChainAbility extends CardAbility {

	public static final Codec<ChainAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder.create(instance -> instance
							.group(GameUtil.TRIGGERS_CODEC.fieldOf("triggers").forGetter(CardAbility::getTriggers),
									ExtraCodecs.nonEmptyList(Codec.list(CardAbility.CODEC)).fieldOf("abilities")
											.forGetter(ChainAbility::getAbilities))
							.apply(instance, ChainAbility::new)));

	private final List<CardAbility> abilities;

	public ChainAbility(Set<CardAbilityTrigger> triggers, List<CardAbility> abilities) {
		super(triggers);
		this.abilities = abilities;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.CHAIN.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		var text = TextComponent.EMPTY.copy();
		text.append(GameUtil.emphasize(GameUtil.triggersToText(triggers)));
		for (var ability : abilities) {
			text.append(ability.getDescription()).append(" ");
		}
		return new Object[] { text };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			List<Card> collected, ItemStack icon) {
		for (var ability : abilities)
			ability.invoke(receivers, state, card, other, collected, icon);
	}

	@Override
	public void trigger(CardAbilityTrigger trigger, List<Receiver> receivers, PlayerState state, Card card, Card target,
			ItemStack icon) {
		if (triggers.contains(trigger)) {
			invoke(receivers, state, card, target, new ArrayList<>(), icon);
		}
	}

	public List<CardAbility> getAbilities() {
		return abilities;
	}

}
