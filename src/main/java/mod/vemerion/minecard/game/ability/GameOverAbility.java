package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.world.item.ItemStack;

public class GameOverAbility extends CardAbility {

	public static final Codec<GameOverAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(GameUtil.TRIGGERS_CODEC.fieldOf("triggers").forGetter(CardAbility::getTriggers),
					Codec.STRING.fieldOf("text_key").forGetter(CardAbility::getTextKey))
			.apply(instance, GameOverAbility::new));

	public GameOverAbility(Set<CardAbilityTrigger> triggers, String textKey) {
		super(triggers, textKey);
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.GAME_OVER.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected, ItemStack icon) {
		state.setGameOver();
	}

}
