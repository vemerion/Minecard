package mod.vemerion.minecard.network;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class EndTurnMessage extends ClientToServerMessage {

	public EndTurnMessage(BlockPos pos) {
		super(pos);
	}

	public static EndTurnMessage decode(final FriendlyByteBuf buffer) {
		return new EndTurnMessage(buffer.readBlockPos());
	}

	@Override
	protected void handle(GameBlockEntity game, ServerPlayer sender) {
		game.endTurn();
	}

}
