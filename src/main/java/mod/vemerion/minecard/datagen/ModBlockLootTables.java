package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.init.ModBlocks;
import net.minecraft.data.loot.BlockLoot;
import net.minecraft.world.level.block.Block;

public class ModBlockLootTables extends BlockLoot {

	@Override
	protected Iterable<Block> getKnownBlocks() {
		return () -> ModBlocks.BLOCKS.getEntries().stream().map(a -> a.get()).iterator();
	}

	@Override
	protected void addTables() {
		dropSelf(ModBlocks.GAME.get());
	}
}
