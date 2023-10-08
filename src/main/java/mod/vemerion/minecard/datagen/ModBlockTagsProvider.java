package mod.vemerion.minecard.datagen;

import java.util.concurrent.CompletableFuture;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.init.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModBlockTagsProvider extends BlockTagsProvider {

	public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider,
			ExistingFileHelper existingFileHelper) {
		super(output, lookupProvider, Main.MODID, existingFileHelper);
	}

	@Override
	protected void addTags(Provider pProvider) {
		tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.GAME.get());
	}

}
