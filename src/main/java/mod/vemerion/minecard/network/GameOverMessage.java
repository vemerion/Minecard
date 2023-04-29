package mod.vemerion.minecard.network;

import net.minecraft.network.FriendlyByteBuf;

public class GameOverMessage extends ServerToClientMessage {

	@Override
	public void encode(final FriendlyByteBuf buffer) {
	}

	public static GameOverMessage decode(final FriendlyByteBuf buffer) {
		return new GameOverMessage();
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.gameOver();
	}
}
