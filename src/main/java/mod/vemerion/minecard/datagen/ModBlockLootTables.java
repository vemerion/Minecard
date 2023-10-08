package mod.vemerion.minecard.datagen;

import java.util.Set;

import mod.vemerion.minecard.init.ModBlocks;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public class ModBlockLootTables extends BlockLootSubProvider {

	protected ModBlockLootTables() {
		super(Set.of(), FeatureFlags.VANILLA_SET);
	}

	@Override
	protected void generate() {
		dropSelf(ModBlocks.GAME.get());
	}

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return ModBlocks.BLOCKS.getEntries().stream().map(r -> r.get()).toList();
	}
}