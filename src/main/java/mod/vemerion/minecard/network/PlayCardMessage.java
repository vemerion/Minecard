package mod.vemerion.minecard.network;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PlayCardMessage extends ClientToServerMessage {

	private int cardId;
	private int leftId;

	public PlayCardMessage(BlockPos pos, int cardId, int leftId) {
		super(pos);
		this.cardId = cardId;
		this.leftId = leftId;
	}

	@Override
	protected void encodeAdditional(FriendlyByteBuf buffer) {
		buffer.writeInt(cardId);
		buffer.writeInt(leftId);
	}

	public static PlayCardMessage decode(final FriendlyByteBuf buffer) {
		return new PlayCardMessage(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
	}

	@Override
	protected void handle(GameBlockEntity game, ServerPlayer sender) {
		game.playCard(cardId, leftId);
	}
}
