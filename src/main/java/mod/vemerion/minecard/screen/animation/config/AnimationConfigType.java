package mod.vemerion.minecard.screen.animation.config;

import com.mojang.serialization.Codec;

public class AnimationConfigType<T extends AnimationConfig> {
	private final Codec<T> codec;

	public AnimationConfigType(Codec<T> codec) {
		this.codec = codec;
	}

	Codec<T> codec() {
		return codec;
	}
}
