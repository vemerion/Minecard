package mod.vemerion.minecard.datagen;

import java.util.List;
import java.util.Set;

import mod.vemerion.minecard.Main;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ForgeAdvancementProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.MOD, modid = Main.MODID)
public class EventSubscriber {

	@SubscribeEvent
	public static void onGatherData(GatherDataEvent event) {
		var generator = event.getGenerator();
		var output = generator.getPackOutput();
		var existingFileHelper = event.getExistingFileHelper();
		var lookup = event.getLookupProvider();

		generator.addProvider(event.includeServer(), new ModLootModifierProvider(output));
		generator.addProvider(event.includeServer(), new ModRecipeProvider(output));
		generator.addProvider(event.includeServer(), new ModBlockTagsProvider(output, lookup, existingFileHelper));
		generator.addProvider(event.includeServer(), new ModCardProvider(output));
		generator.addProvider(event.includeServer(), new ModCardPropertyProvider(output));
		generator.addProvider(event.includeServer(), new ForgeAdvancementProvider(output, lookup, existingFileHelper,
				List.of(new ModAdvancementProvider())));
		generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
				List.of(new LootTableProvider.SubProviderEntry(ModBlockLootTables::new, LootContextParamSets.BLOCK))));
		generator.addProvider(event.includeClient(), new ModItemModelProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ModLanguageProvider(output));
		generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFileHelper));
		generator.addProvider(event.includeClient(), new ModAnimationConfigProvider(output));
	}
}