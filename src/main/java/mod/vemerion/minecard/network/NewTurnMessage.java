package mod.vemerion.minecard.network;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

public class NewTurnMessage extends ServerToClientMessage {

	private UUID current;

	public NewTurnMessage(UUID current) {
		this.current = current;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(current);
	}

	public static NewTurnMessage decode(final FriendlyByteBuf buffer) {
		return new NewTurnMessage(buffer.readUUID());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.setCurrent(current);
	}
}
