package mod.vemerion.minecard.network;

import java.util.function.Supplier;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public abstract class ClientToServerMessage {

	private BlockPos pos;

	public ClientToServerMessage(BlockPos pos) {
		this.pos = pos;
	}

	public final void encode(final FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		encodeAdditional(buffer);
	}

	protected void encodeAdditional(final FriendlyByteBuf buffer) {

	}

	protected abstract void handle(GameBlockEntity game, ServerPlayer sender);
	
	protected boolean canAlwaysBeReceived() {
		return false;
	}

	public final void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);

		context.enqueueWork(() -> {
			var sender = context.getSender();
			var level = sender.getLevel();
			if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof GameBlockEntity game
					&& game.canReceiveMessage(sender) && (game.isPlayerTurn(sender) || canAlwaysBeReceived())) {
				handle(game, sender);
			}
		});
	}
}
