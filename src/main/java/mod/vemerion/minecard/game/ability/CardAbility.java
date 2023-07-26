package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public abstract class CardAbility {

	public static final Codec<CardAbility> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ModCardAbilities.getRegistry()
			.getCodec().dispatch("type", CardAbility::getType, CardAbilityType::codec));

	private static final Object[] NO_ARGS = {};

	protected final Set<CardAbilityTrigger> triggers;
	private Component description;

	public CardAbility(Set<CardAbilityTrigger> triggers) {
		this.triggers = triggers;
	}

	protected abstract CardAbilityType<?> getType();

	protected Object[] getDescriptionArgs() {
		return NO_ARGS;
	}

	protected abstract void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			List<Card> collected, ItemStack icon);

	public Component getDescription() {
		if (description == null) {
			description = new TranslatableComponent(getType().getTranslationKey(), getDescriptionArgs());
		}
		return description;
	}

	public Set<CardAbilityTrigger> getTriggers() {
		return triggers;
	}

	public void trigger(CardAbilityTrigger trigger, List<Receiver> receivers, PlayerState state, Card card, Card target,
			ItemStack icon) {
		if (triggers.contains(trigger)) {
			invoke(receivers, state, card, target, new ArrayList<>(), icon);
		}
	}
}
