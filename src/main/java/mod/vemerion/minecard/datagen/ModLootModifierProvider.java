package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.init.ModLootModifiers;
import mod.vemerion.minecard.lootmodifier.CardLootModifier;
import mod.vemerion.minecard.lootmodifier.CardTreasureLootModifier;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.predicates.AlternativeLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

public class ModLootModifierProvider extends GlobalLootModifierProvider {

	public ModLootModifierProvider(DataGenerator gen) {
		super(gen, Main.MODID);
	}

	@Override
	protected void start() {
		add(ModLootModifiers.CARD.get().getRegistryName().getPath(), ModLootModifiers.CARD.get(), new CardLootModifier(
				new LootItemCondition[] { LootItemKilledByPlayerCondition.killedByPlayer().build() }));

		var builder = SimpleWeightedRandomList.<ResourceLocation>builder();
		for (var spell : Cards.SPELLS) {
			builder.add(spell, 1);
		}
		add(ModLootModifiers.CARD_TREASURE.get().getRegistryName().getPath(), ModLootModifiers.CARD_TREASURE.get(),
				new CardTreasureLootModifier(
						new LootItemCondition[] { LootItemRandomChanceCondition.randomChance(0.2f).build(),
								tables(BuiltInLootTables.ABANDONED_MINESHAFT, BuiltInLootTables.BASTION_TREASURE,
										BuiltInLootTables.BURIED_TREASURE, BuiltInLootTables.DESERT_PYRAMID,
										BuiltInLootTables.END_CITY_TREASURE, BuiltInLootTables.JUNGLE_TEMPLE,
										BuiltInLootTables.NETHER_BRIDGE, BuiltInLootTables.SIMPLE_DUNGEON,
										BuiltInLootTables.STRONGHOLD_CORRIDOR, BuiltInLootTables.WOODLAND_MANSION) },
						builder.build()));

	}

	private LootItemCondition tables(ResourceLocation... tables) {
		var builder = new AlternativeLootItemCondition.Builder();
		for (var table : tables) {
			builder.or(LootTableIdCondition.builder(table));
		}
		return builder.build();
	}
}
