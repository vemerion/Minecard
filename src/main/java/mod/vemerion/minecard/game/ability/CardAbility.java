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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;

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

	protected abstract void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other);

	public Component getDescription() {
		if (description == null) {
			description = new TranslatableComponent(getType().getTranslationKey(), getDescriptionArgs());
		}
		return description;
	}

	public Set<CardAbilityTrigger> getTriggers() {
		return triggers;
	}

	public void createChoices(List<Receiver> receivers, PlayerState state, Card card) {

	}

	public void onSummon(List<Receiver> receivers, PlayerState state, Card card) {
		if (triggers.contains(CardAbilityTrigger.SUMMON)) {
			invoke(receivers, state, card, null);
		}
	}

	public void onAttack(List<Receiver> receivers, PlayerState state, Card card, Card target) {
		if (triggers.contains(CardAbilityTrigger.ATTACK)) {
			invoke(receivers, state, card, target);
		}
	}

	public void onDeath(List<Receiver> receivers, PlayerState state, Card card) {
		if (triggers.contains(CardAbilityTrigger.DEATH)) {
			invoke(receivers, state, card, null);
		}
	}

	public void onHurt(List<Receiver> receivers, PlayerState state, Card card) {
		if (triggers.contains(CardAbilityTrigger.HURT)) {
			invoke(receivers, state, card, null);
		}
	}

	public void onTick(List<Receiver> receivers, PlayerState state, Card card) {
		if (triggers.contains(CardAbilityTrigger.TICK)) {
			invoke(receivers, state, card, null);
		}
	}

	public void onGrow(List<Receiver> receivers, PlayerState state, Card card) {
		if (triggers.contains(CardAbilityTrigger.GROW)) {
			invoke(receivers, state, card, null);
		}
	}

}
