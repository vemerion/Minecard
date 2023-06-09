package mod.vemerion.minecard.screen.animation.config;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.EntityAnimation;
import mod.vemerion.minecard.screen.animation.ParticlesAnimation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec2;

public class EnderDragonAnimationConfig extends AnimationConfig {

	public static final EnderDragonAnimationConfig INSTANCE = new EnderDragonAnimationConfig();

	public static final Codec<EnderDragonAnimationConfig> CODEC = Codec.unit(INSTANCE);

	private EnderDragonAnimationConfig() {
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.ENDER_DRAGON.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		if (targets.isEmpty())
			return;
		var target = targets.get(0).getPosition();
		var start = new Vec2(-50, target.y + ClientCard.CARD_HEIGHT / 2);
		var end = new Vec2(game.width + 50, target.y + ClientCard.CARD_HEIGHT / 2);
		game.addAnimation(new EntityAnimation(game.getMinecraft(), start, () -> end, EntityType.ENDER_DRAGON, 60, 6, 1,
				Optional.of(SoundEvents.ENDER_DRAGON_GROWL), Optional.of(SoundEvents.ENDER_DRAGON_FLAP),
				Optional.empty(), () -> {
				}));
		new PotionAnimationConfig(new ParticlesAnimation.Color(0.8f, 0, 0.9f)).invoke(game, origin, targets);
	}
}
