package mod.vemerion.minecard.screen.animation.config;

import java.util.List;
import java.util.Random;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.AABB;

public abstract class AnimationConfig {
	public static final Codec<AnimationConfig> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ModAnimationConfigs
			.getRegistry().getCodec().dispatch("type", AnimationConfig::getType, AnimationConfigType::codec));

	protected Random random = new Random();

	protected abstract AnimationConfigType<?> getType();

	public abstract void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets);

	protected static AABB calcArea(List<ClientCard> cards) {
		if (cards.isEmpty())
			return new AABB(0, 0, 0, 0, 0, 0);

		var area = fromCard(cards.get(0));

		for (var card : cards)
			area = area.minmax(fromCard(card));

		return area;
	}

	protected static AABB fromCard(ClientCard card) {
		var p = card.getPosition();
		return new AABB(p.x, p.y, 0, p.x + ClientCard.CARD_WIDTH, p.y + ClientCard.CARD_HEIGHT, 0);
	}
}
