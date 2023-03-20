package mod.vemerion.minecard.network;

import java.util.function.Supplier;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class PlayCardMessage {

	private BlockPos pos;
	int card;
	int position;

	public PlayCardMessage(BlockPos pos, int card, int position) {
		this.pos = pos;
		this.card = card;
		this.position = position;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeInt(card);
		buffer.writeInt(position);
	}

	public static PlayCardMessage decode(final FriendlyByteBuf buffer) {
		return new PlayCardMessage(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);

		var sender = context.getSender();
		var level = sender.getLevel();
		if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof GameBlockEntity game) {
			game.playCard(sender, card, position);
		}
	}
}
