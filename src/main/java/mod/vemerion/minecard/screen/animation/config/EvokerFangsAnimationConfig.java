package mod.vemerion.minecard.screen.animation.config;

import java.util.List;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.EvokerFangsAnimation;

public class EvokerFangsAnimationConfig extends AnimationConfig {

	public static final EvokerFangsAnimationConfig INSTANCE = new EvokerFangsAnimationConfig();

	public static final Codec<EvokerFangsAnimationConfig> CODEC = Codec.unit(INSTANCE);

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.EVOKER_FANGS.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		for (var target : targets)
			game.addAnimation(new EvokerFangsAnimation(game.getMinecraft(), target, () -> {
			}));
	}
}
