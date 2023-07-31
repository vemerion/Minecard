package mod.vemerion.minecard.screen.animation.config;

import java.util.List;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.WitherAnimation;

public class WitherAnimationConfig extends AnimationConfig {

	public static final WitherAnimationConfig INSTANCE = new WitherAnimationConfig();

	public static final Codec<WitherAnimationConfig> CODEC = Codec.unit(INSTANCE);

	private WitherAnimationConfig() {
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.WITHER.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		game.addAnimation(new WitherAnimation(game.getMinecraft()));
	}
}
