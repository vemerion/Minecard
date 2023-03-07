package mod.vemerion.minecard.block;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.OpenGameMessage;
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
import net.minecraftforge.network.PacketDistributor;

public class GameBlock extends Block implements EntityBlock {
	public GameBlock(Properties properties) {
		super(properties);
	}

	public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand,
			BlockHitResult pHit) {
		if (!pLevel.isClientSide) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) pPlayer), new OpenGameMessage());
		}
		return InteractionResult.sidedSuccess(pLevel.isClientSide);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
		return new GameBlockEntity(pPos, pState);
	}
}
