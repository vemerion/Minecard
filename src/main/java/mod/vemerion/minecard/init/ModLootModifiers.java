package mod.vemerion.minecard.init;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.lootmodifier.CardLootModifier;
import mod.vemerion.minecard.lootmodifier.CardTreasureLootModifier;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModLootModifiers {
	public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIERS = DeferredRegister
			.create(ForgeRegistries.Keys.LOOT_MODIFIER_SERIALIZERS, Main.MODID);

	public static final RegistryObject<GlobalLootModifierSerializer<CardLootModifier>> CARD = LOOT_MODIFIERS
			.register("card", () -> new CardLootModifier.Serializer());
	public static final RegistryObject<GlobalLootModifierSerializer<CardTreasureLootModifier>> CARD_TREASURE = LOOT_MODIFIERS
			.register("card_treasure", () -> new CardTreasureLootModifier.Serializer());

}
