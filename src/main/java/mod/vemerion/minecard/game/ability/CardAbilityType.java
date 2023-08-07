package mod.vemerion.minecard.game.ability;

import com.mojang.serialization.Codec;

import net.minecraftforge.registries.ForgeRegistryEntry;

public class CardAbilityType<T extends CardAbility> extends ForgeRegistryEntry<CardAbilityType<?>> {
	private final Codec<T> codec;

	public CardAbilityType(Codec<T> codec) {
		this.codec = codec;
	}

	Codec<T> codec() {
		return codec;
	}
}
