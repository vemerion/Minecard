package mod.vemerion.minecard.network;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PlayerChoiceResponseMessage extends ClientToServerMessage {

	private int selected;

	public PlayerChoiceResponseMessage(BlockPos pos, int selected) {
		super(pos);
		this.selected = selected;
	}

	@Override
	protected void encodeAdditional(FriendlyByteBuf buffer) {
		buffer.writeInt(selected);
	}

	public static PlayerChoiceResponseMessage decode(final FriendlyByteBuf buffer) {
		return new PlayerChoiceResponseMessage(buffer.readBlockPos(), buffer.readInt());
	}

	@Override
	protected void handle(GameBlockEntity game, ServerPlayer sender) {
		game.choice(selected);
	}
}
