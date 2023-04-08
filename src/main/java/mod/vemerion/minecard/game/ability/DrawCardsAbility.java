package mod.vemerion.minecard.game.ability;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.server.level.ServerPlayer;

public class DrawCardsAbility extends CardAbility {

	public static final Codec<DrawCardsAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger),
					Codec.INT.fieldOf("count").forGetter(DrawCardsAbility::getCount))
			.apply(instance, DrawCardsAbility::new));

	private final int count;

	public DrawCardsAbility(CardAbilityTrigger trigger, int count) {
		super(trigger);
		this.count = count;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.DRAW_CARDS.get();
	}

	@Override
	protected void invoke(List<ServerPlayer> receivers, PlayerState state, Card card) {
		state.drawCards(receivers, count);
	}

	public int getCount() {
		return count;
	}

}
