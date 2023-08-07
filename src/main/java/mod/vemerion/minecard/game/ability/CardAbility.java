package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public abstract class CardAbility {

	public static final Codec<CardAbility> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ModCardAbilities.getRegistry()
			.getCodec().dispatch("type", CardAbility::getType, CardAbilityType::codec));

	protected final Set<CardAbilityTrigger> triggers;
	protected final String textKey;

	public CardAbility(Set<CardAbilityTrigger> triggers, String textKey) {
		this.triggers = triggers;
		this.textKey = textKey;
	}

	protected abstract CardAbilityType<?> getType();

	protected abstract void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected, ItemStack icon);

	public Component getText() {
		return textKey.isEmpty() ? TextComponent.EMPTY : new TranslatableComponent(textKey);
	}

	public Set<CardAbilityTrigger> getTriggers() {
		return triggers;
	}

	public String getTextKey() {
		return textKey;
	}

	public void trigger(CardAbilityTrigger trigger, List<Receiver> receivers, PlayerState state, Card card, Card target,
			ItemStack icon) {
		if (triggers.contains(trigger)) {
			invoke(receivers, state, card, target, new Collected(), icon);
		}
	}
}
