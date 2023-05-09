package mod.vemerion.minecard.network;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PlayerChoiceResponseMessage extends ClientToServerMessage {

	private int id;
	private int selected;

	public PlayerChoiceResponseMessage(BlockPos pos, int id, int selected) {
		super(pos);
		this.id = id;
		this.selected = selected;
	}

	@Override
	protected void encodeAdditional(FriendlyByteBuf buffer) {
		buffer.writeInt(id);
		buffer.writeInt(selected);
	}

	public static PlayerChoiceResponseMessage decode(final FriendlyByteBuf buffer) {
		return new PlayerChoiceResponseMessage(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
	}

	@Override
	protected void handle(GameBlockEntity game, ServerPlayer sender) {
		game.choice(id, selected);
	}
}
