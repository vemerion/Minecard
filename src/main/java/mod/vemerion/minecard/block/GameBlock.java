package mod.vemerion.minecard.block;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import mod.vemerion.minecard.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GameBlock extends Block implements EntityBlock {
	private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 15.0D, 16.0D);

	public GameBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
		return SHAPE;
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

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState,
			BlockEntityType<T> pBlockEntityType) {
		return pLevel.isClientSide ? null
				: pBlockEntityType == ModBlockEntities.GAME.get()
						? (level, pos, state, tileEntity) -> ((GameBlockEntity) tileEntity).tick()
						: null;
	}

	@Override
	public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 20;
	}

	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
		return 5;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
		pLevel.getBlockEntity(pPos, ModBlockEntities.GAME.get()).ifPresent(game -> {
			game.exit();
		});
		super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
	}
}
