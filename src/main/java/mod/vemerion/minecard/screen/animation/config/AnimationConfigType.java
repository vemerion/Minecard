package mod.vemerion.minecard.screen.animation.config;

import com.mojang.serialization.Codec;

import net.minecraftforge.registries.ForgeRegistryEntry;

public class AnimationConfigType<T extends AnimationConfig> extends ForgeRegistryEntry<AnimationConfigType<?>> {
	private final Codec<T> codec;

	public AnimationConfigType(Codec<T> codec) {
		this.codec = codec;
	}

	Codec<T> codec() {
		return codec;
	}
}
