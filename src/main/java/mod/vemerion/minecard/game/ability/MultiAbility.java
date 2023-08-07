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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class MultiAbility extends CardAbility {

	public static final Codec<MultiAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder.create(instance -> instance
							.group(Codec.STRING.optionalFieldOf("text_key", "").forGetter(CardAbility::getTextKey),
									ExtraCodecs.nonEmptyList(Codec.list(CardAbility.CODEC)).fieldOf("abilities")
											.forGetter(MultiAbility::getAbilities))
							.apply(instance, MultiAbility::new)));

	private final List<CardAbility> abilities;

	public MultiAbility(String textKey, List<CardAbility> abilities) {
		super(EnumSet.allOf(CardAbilityTrigger.class), textKey);
		this.abilities = abilities;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.MULTI.get();
	}

	@Override
	public Component getText() {
		if (getTextKey().isEmpty()) {
			var text = TextComponent.EMPTY.copy();
			int i = 0;
			for (var ability : abilities) {
				text.append(ability.getText());
				if (i < abilities.size() - 1)
					text.append("\n");
				i++;
			}
			return text;
		}
		return super.getText();
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
