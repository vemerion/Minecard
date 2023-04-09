package mod.vemerion.minecard.game;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import mod.vemerion.minecard.Main;
import net.minecraft.resources.ResourceLocation;

public class LazyCardType {

	public static final Codec<LazyCardType> CODEC = Codec.either(ResourceLocation.CODEC, CardType.CODEC).xmap(
			either -> either.map(LazyCardType::new, LazyCardType::new),
			lazy -> lazy.location != null ? Either.left(lazy.location) : Either.right(lazy.cardType));

	private ResourceLocation location;
	private CardType cardType;

	public LazyCardType(ResourceLocation location) {
		Preconditions.checkNotNull(location, "ResourceLocation for LazyCardType cannot be null.");
		this.location = location;
	}

	public LazyCardType(CardType cardType) {
		Preconditions.checkNotNull(cardType, "CardType for LazyCardType cannot be null.");
		this.cardType = cardType;
	}

	public CardType get(boolean isClient) {
		if (cardType == null) {
			cardType = Cards.getInstance(isClient).get(location);
			if (cardType == null) {
				Main.LOGGER.error("Could not find card '" + location + "'.");
			}
		}

		return cardType;
	}
}
