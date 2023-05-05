package mod.vemerion.minecard.screen.animation.config;

import java.util.List;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.LightningBoltAnimation;

public class LightningBoltAnimationConfig extends AnimationConfig {

	public static final LightningBoltAnimationConfig INSTANCE = new LightningBoltAnimationConfig();

	public static final Codec<LightningBoltAnimationConfig> CODEC = Codec.unit(INSTANCE);

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.LIGHTNING_BOLT.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		for (var target : targets)
			game.addAnimation(new LightningBoltAnimation(game.getMinecraft(), target, () -> {
			}));
	}
}
