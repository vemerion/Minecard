package mod.vemerion.minecard.screen.animation.config;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.ParticlesAnimation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec2;

public class SonicBoomAnimationConfig extends AnimationConfig {

	public static final SonicBoomAnimationConfig INSTANCE = new SonicBoomAnimationConfig();

	public static final Codec<SonicBoomAnimationConfig> CODEC = Codec.unit(INSTANCE);

	private SonicBoomAnimationConfig() {

	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.SONIC_BOOM.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		if (targets.isEmpty())
			return;

		var particleConfig = new ParticlesAnimation.ParticleConfig(new ParticlesAnimation.Color(0.0f, 0.8f, 0.8f), 100,
				100.1f, 0, 0, ParticlesAnimation.ParticleConfig.SONIC_BOOM_TEXTURES);
		game.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WARDEN_SONIC_BOOM, 1));
		var direction = targets.get(0).getDestination().add(origin.getDestination().negated());
		var i = new AtomicInteger();
		game.addAnimation(new ParticlesAnimation(game.getMinecraft(),
				() -> origin.getDestination().add(new Vec2(ClientCard.CARD_WIDTH / 2, ClientCard.CARD_HEIGHT / 2))
						.add(direction.scale((i.getAndIncrement()) * 0.2f)),
				5, 1, particleConfig, () -> {
				}));
	}
}
