package mod.vemerion.minecard.network;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PlayCardMessage extends ClientToServerMessage {

	private int cardId;
	private int position;

	public PlayCardMessage(BlockPos pos, int cardId, int position) {
		super(pos);
		this.cardId = cardId;
		this.position = position;
	}

	@Override
	protected void encodeAdditional(FriendlyByteBuf buffer) {
		buffer.writeInt(cardId);
		buffer.writeInt(position);
	}

	public static PlayCardMessage decode(final FriendlyByteBuf buffer) {
		return new PlayCardMessage(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
	}

	@Override
	protected void handle(GameBlockEntity game, ServerPlayer sender) {
		game.playCard(sender, cardId, position);
	}
}
