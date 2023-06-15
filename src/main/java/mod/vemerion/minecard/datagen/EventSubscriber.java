package mod.vemerion.minecard.datagen;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import mod.vemerion.minecard.Main;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

@EventBusSubscriber(bus = Bus.MOD, modid = Main.MODID)
public class EventSubscriber {

	@SubscribeEvent
	public static void onGatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();

		ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

		if (event.includeServer()) {
			generator.addProvider(new ModLootModifierProvider(generator));
			generator.addProvider(new ModRecipeProvider(generator));
			generator.addProvider(new ModBlockTagsProvider(generator, existingFileHelper));
			generator.addProvider(new ModCardProvider(generator));
			generator.addProvider(new ModCardPropertyProvider(generator));
			generator.addProvider(new LootTableProvider(generator) {
				@Override
				protected List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, Builder>>>, LootContextParamSet>> getTables() {
					return ImmutableList.of(Pair.of(ModBlockLootTables::new, LootContextParamSets.BLOCK));
				};

				@Override
				protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
				}
			});
		}
		if (event.includeClient()) {
			generator.addProvider(new ModItemModelProvider(generator, existingFileHelper));
			generator.addProvider(new ModLanguageProvider(generator));
			generator.addProvider(new ModBlockStateProvider(generator, existingFileHelper));
			generator.addProvider(new ModAnimationConfigProvider(generator));
		}
	}
}
