package mod.vemerion.minecard.screen.animation.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.ParticlesAnimation;

public class SplashAnimationConfig extends AnimationConfig {

	public static final Codec<SplashAnimationConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ParticlesAnimation.Color.CODEC.fieldOf("color").forGetter(SplashAnimationConfig::getColor))
			.apply(instance, SplashAnimationConfig::new));

	private final ParticlesAnimation.Color color;

	public SplashAnimationConfig(ParticlesAnimation.Color color) {
		this.color = color;
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.SPLASH.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		if (targets.isEmpty())
			return;

		var particleConfig = new ParticlesAnimation.ParticleConfig(color, 5, 10, 1f, 1.5f,
				ParticlesAnimation.ParticleConfig.GENERIC_REVERSE_TEXTURES);
		targets.remove(origin);
		var area = calcArea(targets);
		game.addAnimation(
				new ParticlesAnimation(game.getMinecraft(), () -> randomInAABB(area), 15, 1, particleConfig, () -> {
				}));
	}

	public ParticlesAnimation.Color getColor() {
		return color;
	}
}
