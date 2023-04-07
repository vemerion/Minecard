package mod.vemerion.minecard.network;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class CloseGameMessage extends ClientToServerMessage {

	public CloseGameMessage(BlockPos pos) {
		super(pos);
	}

	public static CloseGameMessage decode(final FriendlyByteBuf buffer) {
		return new CloseGameMessage(buffer.readBlockPos());
	}

	@Override
	protected void handle(GameBlockEntity game, ServerPlayer sender) {
		game.closeGame(sender);
	}

	@Override
	protected boolean canAlwaysBeReceived() {
		return true;
	}

}
