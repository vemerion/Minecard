package mod.vemerion.minecard.network;

import java.util.List;
import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

public class SetReadyMessage extends ServerToClientMessage {

	private UUID id;
	private List<Integer> cards;

	public SetReadyMessage(UUID id, List<Integer> cards) {
		this.id = id;
		this.cards = cards;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeCollection(cards, (b, c) -> b.writeInt(c));
	}

	public static SetReadyMessage decode(final FriendlyByteBuf buffer) {
		return new SetReadyMessage(buffer.readUUID(), buffer.readList(b -> b.readInt()));
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.setReady(id, cards);
	}
}
