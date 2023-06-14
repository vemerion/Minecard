package mod.vemerion.minecard.game.ability;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;

public class ChanceAbility extends CardAbility {

	public static final Codec<ChanceAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(Codec.INT.fieldOf("chance").forGetter(ChanceAbility::getChance),
							CardAbility.CODEC.fieldOf("ability").forGetter(ChanceAbility::getAbility))
					.apply(instance, ChanceAbility::new)));

	private final int chance;
	private final CardAbility ability;

	public ChanceAbility(int chance, CardAbility ability) {
		super(ability.getTrigger());
		this.chance = chance;
		this.ability = ability;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.CHANCE.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		return new Object[] { chance,
				GameUtil.emphasize(
						new TranslatableComponent(ModCardAbilities.CHANCE.get().getTranslationKey() + ".chance")),
				ability.getDescription() };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other) {
		if (state.getGame().getRandom().nextInt(100) < chance) {
			ability.invoke(receivers, state, card, other);
		}
	}

	public int getChance() {
		return chance;
	}

	public CardAbility getAbility() {
		return ability;
	}

}
