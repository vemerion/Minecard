package mod.vemerion.minecard.blockentity;

import mod.vemerion.minecard.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GameBlockEntity extends BlockEntity {

	public GameBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(ModBlockEntities.GAME.get(), pWorldPosition, pBlockState);
	}
}