package mod.vemerion.minecard.game.ability;

import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class MultiAbility extends CardAbility {

	public static final Codec<MultiAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder
							.create(instance -> instance
									.group(ExtraCodecs.nonEmptyList(Codec.list(CardAbility.CODEC)).fieldOf("abilities")
											.forGetter(MultiAbility::getAbilities))
									.apply(instance, MultiAbility::new)));

	private final List<CardAbility> abilities;

	public MultiAbility(List<CardAbility> abilities) {
		super(EnumSet.allOf(CardAbilityTrigger.class));
		this.abilities = abilities;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.MULTI.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		var text = TextComponent.EMPTY.copy();
		int i = 0;
		for (var ability : abilities) {
			text.append(ability.getDescription());
			if (i < abilities.size() - 1)
				text.append("\n");
			i++;
		}
		return new Object[] { text };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected, ItemStack icon) {
	}

	@Override
	public void trigger(CardAbilityTrigger trigger, List<Receiver> receivers, PlayerState state, Card card, Card target,
			ItemStack icon) {
		for (var ability : abilities)
			ability.trigger(trigger, receivers, state, card, target, icon);
	}

	public List<CardAbility> getAbilities() {
		return abilities;
	}

}
