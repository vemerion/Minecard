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
		for (var ability : abilities) {
			text.append(ability.getDescription());
			text.append("\n");
		}
		return new Object[] { text };
	}

	@Override
	public void createChoices(List<Receiver> receivers, PlayerState state, Card card) {
		for (var ability : abilities)
			ability.createChoices(receivers, state, card);
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other) {
	}

	@Override
	public void onSummon(List<Receiver> receivers, PlayerState state, Card card) {
		for (var ability : abilities)
			ability.onSummon(receivers, state, card);
	}

	@Override
	public void onAttack(List<Receiver> receivers, PlayerState state, Card card, Card target) {
		for (var ability : abilities)
			ability.onAttack(receivers, state, card, target);
	}

	@Override
	public void onDeath(List<Receiver> receivers, PlayerState state, Card card) {
		for (var ability : abilities)
			ability.onDeath(receivers, state, card);
	}

	@Override
	public void onHurt(List<Receiver> receivers, PlayerState state, Card card) {
		for (var ability : abilities)
			ability.onHurt(receivers, state, card);
	}

	@Override
	public void onTick(List<Receiver> receivers, PlayerState state, Card card) {
		for (var ability : abilities)
			ability.onTick(receivers, state, card);
	}

	@Override
	public void onGrow(List<Receiver> receivers, PlayerState state, Card card) {
		for (var ability : abilities)
			ability.onGrow(receivers, state, card);
	}

	public List<CardAbility> getAbilities() {
		return abilities;
	}

}
