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

public class GameOverAbility extends CardAbility {

	public static final Codec<GameOverAbility> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger))
					.apply(instance, GameOverAbility::new));

	public GameOverAbility(CardAbilityTrigger trigger) {
		super(trigger);
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.GAME_OVER.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		return new Object[] { GameUtil.emphasize(trigger.getText()) };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other) {
		state.getGame().setGameOver();
	}

}
