package mod.vemerion.minecard.game.ability;

import java.util.List;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public abstract class CardAbility {

	public static final Codec<CardAbility> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ModCardAbilities.getRegistry()
			.getCodec().dispatch("type", CardAbility::getType, CardAbilityType::codec));

	private final CardAbilityTrigger trigger;

	public CardAbility(CardAbilityTrigger trigger) {
		this.trigger = trigger;
	}

	protected abstract CardAbilityType<?> getType();

	protected abstract void invoke(List<ServerPlayer> receivers, PlayerState state, Card card);

	public CardAbilityTrigger getTrigger() {
		return trigger;
	}

	public void onSummon(List<ServerPlayer> receivers, PlayerState state, Card card) {
		if (trigger == CardAbilityTrigger.ALWAYS || trigger == CardAbilityTrigger.SUMMON) {
			invoke(receivers, state, card);
		}
	}

}
