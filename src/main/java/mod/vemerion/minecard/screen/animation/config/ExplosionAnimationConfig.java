package mod.vemerion.minecard.screen.animation.config;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.ParticlesAnimation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;

public class ExplosionAnimationConfig extends AnimationConfig {

	public static final Codec<ExplosionAnimationConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(Codec.BOOL.fieldOf("apply_to_targets").forGetter(ExplosionAnimationConfig::shouldApplyToTargets),
					Codec.BOOL.fieldOf("apply_to_origin").forGetter(ExplosionAnimationConfig::shouldApplyToOrigin))
			.apply(instance, ExplosionAnimationConfig::new));

	private final boolean applyToTargets;
	private final boolean applyToOrigin;

	public ExplosionAnimationConfig(boolean applyToTargets, boolean applyToOrigin) {
		this.applyToTargets = applyToTargets;
		this.applyToOrigin = applyToOrigin;
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.EXPLOSION.get();
	}

	public boolean shouldApplyToTargets() {
		return applyToTargets;
	}

	public boolean shouldApplyToOrigin() {
		return applyToOrigin;
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		var cards = new ArrayList<ClientCard>();
		if (applyToTargets)
			cards.addAll(targets);
		if (applyToOrigin && origin != null)
			cards.add(origin);

		for (var card : cards) {
			var color = random.nextFloat() * 0.6f + 0.4f;
			var particleConfig = new ParticlesAnimation.ParticleConfig(
					new ParticlesAnimation.Color(color, color, color), 40, 60,
					ParticlesAnimation.ParticleConfig.EXPLOSION_TEXTURES);
			game.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_EXPLODE, 1f));
			game.addAnimation(new ParticlesAnimation(game.getMinecraft(), fromCard(card), 7, 1, particleConfig, () -> {
			}));
		}
	}
}
