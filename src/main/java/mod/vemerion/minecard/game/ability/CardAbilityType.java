package mod.vemerion.minecard.game.ability;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.Util;
import net.minecraftforge.registries.ForgeRegistryEntry;

public class CardAbilityType<T extends CardAbility> extends ForgeRegistryEntry<CardAbilityType<?>> {
	private final Codec<T> codec;

	public CardAbilityType(Codec<T> codec) {
		this.codec = codec;
	}

	Codec<T> codec() {
		return codec;
	}

	public String getTranslationKey() {
		return Util.makeDescriptionId(ModCardAbilities.CARD_ABILITIES.getRegistryName().getNamespace(),
				getRegistryName());
	}
}
