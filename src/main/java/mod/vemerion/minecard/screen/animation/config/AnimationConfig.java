package mod.vemerion.minecard.screen.animation.config;

import java.util.List;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.util.ExtraCodecs;

public abstract class AnimationConfig {
	public static final Codec<AnimationConfig> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ModAnimationConfigs
			.getRegistry().getCodec().dispatch("type", AnimationConfig::getType, AnimationConfigType::codec));

	protected abstract AnimationConfigType<?> getType();

	public abstract void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets);
}
