package mod.vemerion.minecard.screen.animation.config;

import java.util.List;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;

public class NoAnimationConfig extends AnimationConfig {

	public static final NoAnimationConfig NO_ANIMATION_CONFIG = new NoAnimationConfig();

	public static final Codec<NoAnimationConfig> CODEC = Codec.unit(NO_ANIMATION_CONFIG);

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.NO_ANIMATION_CONFIG.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		
	}
}
