package mod.vemerion.minecard.network;

import java.util.Map;
import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

public class UpdateDecksMessage extends ServerToClientMessage {

	private Map<UUID, Integer> sizes;

	public UpdateDecksMessage(Map<UUID, Integer> sizes) {
		this.sizes = sizes;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeMap(sizes, FriendlyByteBuf::writeUUID, FriendlyByteBuf::writeInt);
	}

	public static UpdateDecksMessage decode(final FriendlyByteBuf buffer) {
		return new UpdateDecksMessage(buffer.readMap(FriendlyByteBuf::readUUID, FriendlyByteBuf::readInt));
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.updateDecks(sizes);
	}
}
