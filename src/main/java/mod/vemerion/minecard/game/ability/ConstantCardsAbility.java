package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.LazyCardType;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class ConstantCardsAbility extends CardAbility {

	public static final Codec<ConstantCardsAbility> CODEC = RecordCodecBuilder
			.create(instance -> instance.group(ExtraCodecs.nonEmptyList(Codec.list(LazyCardType.CODEC)).fieldOf("cards")
					.forGetter(ConstantCardsAbility::getCards)).apply(instance, ConstantCardsAbility::new));

	private final List<LazyCardType> cards;

	public ConstantCardsAbility(List<LazyCardType> cards) {
		super(Set.of(), "");
		this.cards = cards;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.CONSTANT_CARDS.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected, ItemStack icon) {
		for (var c : cards) {
			collected.get(0).add(c.get(false).create());
		}
	}

	public List<LazyCardType> getCards() {
		return cards;
	}

}
