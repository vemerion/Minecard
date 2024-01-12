package mod.vemerion.minecard.network;

import net.minecraft.network.FriendlyByteBuf;

public class GameOverMessage extends ServerToClientMessage {

	private final boolean defeat;

	public GameOverMessage(boolean defeat) {
		this.defeat = defeat;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeBoolean(defeat);
	}

	public static GameOverMessage decode(final FriendlyByteBuf buffer) {
		return new GameOverMessage(buffer.readBoolean());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.gameOver(defeat);
	}
}
