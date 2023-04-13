package mod.vemerion.minecard.screen.animation.config;

import java.util.List;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.ParticlesAnimation;
import net.minecraft.world.phys.AABB;

public class GlowAnimationConfig extends AnimationConfig {

	public static final GlowAnimationConfig INSTANCE = new GlowAnimationConfig();

	public static final Codec<GlowAnimationConfig> CODEC = Codec.unit(INSTANCE);

	private GlowAnimationConfig() {

	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.GLOW.get();
	}

	private static AABB calcArea(List<ClientCard> cards) {
		if (cards.isEmpty())
			return new AABB(0, 0, 0, 0, 0, 0);

		var area = fromCard(cards.get(0));

		for (var card : cards)
			area = area.minmax(fromCard(card));

		return area;
	}

	private static AABB fromCard(ClientCard card) {
		var p = card.getPosition();
		return new AABB(p.x, p.y, 0, p.x + ClientCard.CARD_WIDTH, p.y + ClientCard.CARD_HEIGHT, 0);
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		if (targets.isEmpty())
			return;

		var particleConfig = new ParticlesAnimation.ParticleConfig(new ParticlesAnimation.Color(0.6f, 1.0f, 0.8f),
				ParticlesAnimation.ParticleConfig.GLOW_TEXTURES);
		targets.remove(origin);
		var area = calcArea(targets);
		int count = (int) ((area.maxX - area.minX) * (area.maxY - area.minY) * 0.0004);
		game.addAnimation(new ParticlesAnimation(game.getMinecraft(), area, count, 20, particleConfig, () -> {
		}));
	}
}
