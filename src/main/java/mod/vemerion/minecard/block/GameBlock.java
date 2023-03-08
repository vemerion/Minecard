package mod.vemerion.minecard.block;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import mod.vemerion.minecard.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GameBlock extends Block implements EntityBlock {
	public GameBlock(Properties properties) {
		super(properties);
	}

	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
			BlockHitResult pHit) {
		if (!pLevel.isClientSide) {
			pLevel.getBlockEntity(pPos, ModBlockEntities.GAME.get()).ifPresent(game -> {
				var stack = pPlayer.getItemInHand(pHand);
				game.open((ServerPlayer) pPlayer, stack);
			});
		}
		return InteractionResult.sidedSuccess(pLevel.isClientSide);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new GameBlockEntity(pPos, pState);
	}
}
