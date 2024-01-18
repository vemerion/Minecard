package mod.vemerion.minecard.screen.animation.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.ParticlesAnimation;

public class PotionAnimationConfig extends AnimationConfig {

	public static final Codec<PotionAnimationConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ParticlesAnimation.Color.CODEC.fieldOf("color").forGetter(PotionAnimationConfig::getColor))
			.apply(instance, PotionAnimationConfig::new));

	private final ParticlesAnimation.Color color;

	public PotionAnimationConfig(ParticlesAnimation.Color color) {
		this.color = color;
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.POTION.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		if (targets.isEmpty())
			return;
		var particleConfig = new ParticlesAnimation.ParticleConfig(color, 15, 25, 0, 0,
				ParticlesAnimation.ParticleConfig.POTION_TEXTURES);
		targets.remove(origin);
		var area = calcArea(targets);
		int count = (int) ((area.maxX - area.minX) * (area.maxY - area.minY) * 0.001);
		game.addAnimation(
				new ParticlesAnimation(game.getMinecraft(), () -> randomInAABB(area), count, 20, particleConfig, () -> {
				}));
	}

	public ParticlesAnimation.Color getColor() {
		return color;
	}
}
