package mod.vemerion.minecard.block;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GameBlock extends Block implements EntityBlock {
	public GameBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new GameBlockEntity(pPos, pState);
	}
}
