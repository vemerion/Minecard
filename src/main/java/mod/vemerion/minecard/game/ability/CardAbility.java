package mod.vemerion.minecard.game.ability;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public abstract class CardAbility {

	public static final Codec<CardAbility> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ModCardAbilities.getRegistry()
			.getCodec().dispatch("type", CardAbility::getType, CardAbilityType::codec));

	private static final Object[] NO_ARGS = {};

	protected final CardAbilityTrigger trigger;
	private Component description;

	public CardAbility(CardAbilityTrigger trigger) {
		this.trigger = trigger;
	}

	protected abstract CardAbilityType<?> getType();

	protected Object[] getDescriptionArgs() {
		return NO_ARGS;
	}

	protected abstract void invoke(List<ServerPlayer> receivers, PlayerState state, Card card, @Nullable Card other);

	public Component getDescription() {
		if (description == null) {
			description = new TranslatableComponent(getType().getTranslationKey(), getDescriptionArgs());
		}
		return description;
	}

	public CardAbilityTrigger getTrigger() {
		return trigger;
	}

	public void onSummon(List<ServerPlayer> receivers, PlayerState state, Card card) {
		if (trigger == CardAbilityTrigger.ALWAYS || trigger == CardAbilityTrigger.SUMMON) {
			invoke(receivers, state, card, null);
		}
	}

	public void onAttack(List<ServerPlayer> receivers, PlayerState state, Card card, Card target) {
		if (trigger == CardAbilityTrigger.ALWAYS || trigger == CardAbilityTrigger.ATTACK) {
			invoke(receivers, state, card, target);
		}
	}

	public void onDeath(List<ServerPlayer> receivers, PlayerState state, Card card) {
		if (trigger == CardAbilityTrigger.ALWAYS || trigger == CardAbilityTrigger.DEATH) {
			invoke(receivers, state, card, null);
		}
	}

	public void onHurt(List<ServerPlayer> receivers, PlayerState state, Card card) {
		if (trigger == CardAbilityTrigger.ALWAYS || trigger == CardAbilityTrigger.HURT) {
			invoke(receivers, state, card, null);
		}
	}

}
