package mod.vemerion.minecard.game.ability;

import com.mojang.serialization.Codec;

public class CardAbilityType<T extends CardAbility> {
	private final Codec<T> codec;

	public CardAbilityType(Codec<T> codec) {
		this.codec = codec;
	}

	Codec<T> codec() {
		return codec;
	}
}
