package mod.vemerion.minecard.init;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.lootmodifier.CardLootModifier;
import mod.vemerion.minecard.lootmodifier.CardTreasureLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS = DeferredRegister
			.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, Main.MODID);

	public static final RegistryObject<Codec<CardLootModifier>> CARD = LOOT_MODIFIERS.register("card",
			() -> CardLootModifier.CODEC);
	public static final RegistryObject<Codec<CardTreasureLootModifier>> CARD_TREASURE = LOOT_MODIFIERS
			.register("card_treasure", () -> CardTreasureLootModifier.CODEC);
}
