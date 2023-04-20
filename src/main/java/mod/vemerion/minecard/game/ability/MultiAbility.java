package mod.vemerion.minecard.game.ability;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

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
		super(CardAbilityTrigger.ALWAYS);
		this.abilities = abilities;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.MULTI.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		var text = TextComponent.EMPTY.copy();
		for (var ability : abilities) {
			text.append(ability.getDescription());
			text.append("\n");
		}
		return new Object[] { text };
	}

	@Override
	protected void invoke(List<ServerPlayer> receivers, PlayerState state, Card card, @Nullable Card other) {
	}

	@Override
	public void onSummon(List<ServerPlayer> receivers, PlayerState state, Card card) {
		if (trigger == CardAbilityTrigger.ALWAYS || trigger == CardAbilityTrigger.SUMMON) {
			for (var ability : abilities)
				ability.onSummon(receivers, state, card);
		}
	}

	@Override
	public void onAttack(List<ServerPlayer> receivers, PlayerState state, Card card, Card target) {
		if (trigger == CardAbilityTrigger.ALWAYS || trigger == CardAbilityTrigger.ATTACK) {
			for (var ability : abilities)
				ability.onAttack(receivers, state, card, target);
		}
	}

	@Override
	public void onDeath(List<ServerPlayer> receivers, PlayerState state, Card card) {
		if (trigger == CardAbilityTrigger.ALWAYS || trigger == CardAbilityTrigger.DEATH) {
			for (var ability : abilities)
				ability.onDeath(receivers, state, card);
		}
	}

	@Override
	public void onHurt(List<ServerPlayer> receivers, PlayerState state, Card card) {
		if (trigger == CardAbilityTrigger.ALWAYS || trigger == CardAbilityTrigger.HURT) {
			for (var ability : abilities)
				ability.onHurt(receivers, state, card);
		}
	}

	@Override
	public void onTick(List<ServerPlayer> receivers, PlayerState state, Card card) {
		if (trigger == CardAbilityTrigger.ALWAYS || trigger == CardAbilityTrigger.TICK) {
			for (var ability : abilities)
				ability.onTick(receivers, state, card);
		}
	}

	public List<CardAbility> getAbilities() {
		return abilities;
	}

}
