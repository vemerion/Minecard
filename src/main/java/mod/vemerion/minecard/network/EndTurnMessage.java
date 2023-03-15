package mod.vemerion.minecard.network;

import java.util.function.Supplier;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class EndTurnMessage {

	private BlockPos pos;

	public EndTurnMessage(BlockPos pos) {
		this.pos = pos;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
	}

	public static EndTurnMessage decode(final FriendlyByteBuf buffer) {
		return new EndTurnMessage(buffer.readBlockPos());
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);

		var sender = context.getSender();
		var level = sender.getLevel();
		if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof GameBlockEntity game) {
			game.endTurn(sender);
		}
	}
}
