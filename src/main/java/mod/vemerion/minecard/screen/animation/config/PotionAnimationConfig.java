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
			.group(Codec.FLOAT.fieldOf("red").forGetter(PotionAnimationConfig::getRed),
					Codec.FLOAT.fieldOf("green").forGetter(PotionAnimationConfig::getGreen),
					Codec.FLOAT.fieldOf("blue").forGetter(PotionAnimationConfig::getBlue))
			.apply(instance, PotionAnimationConfig::new));

	private final float red;
	private final float green;
	private final float blue;

	public PotionAnimationConfig(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.POTION.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		if (targets.isEmpty())
			return;
		var particleConfig = new ParticlesAnimation.ParticleConfig(new ParticlesAnimation.Color(red, green, blue),
				ParticlesAnimation.ParticleConfig.POTION_TEXTURES);
		targets.remove(origin);
		var area = calcArea(targets);
		int count = (int) ((area.maxX - area.minX) * (area.maxY - area.minY) * 0.001);
		game.addAnimation(new ParticlesAnimation(game.getMinecraft(), area, count, 20, particleConfig, () -> {
		}));
	}

	public float getRed() {
		return red;
	}

	public float getGreen() {
		return green;
	}

	public float getBlue() {
		return blue;
	}
}
