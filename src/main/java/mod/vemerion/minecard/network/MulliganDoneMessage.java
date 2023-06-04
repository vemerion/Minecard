package mod.vemerion.minecard.network;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

public class MulliganDoneMessage extends ServerToClientMessage {

	private UUID id;

	public MulliganDoneMessage(UUID id) {
		this.id = id;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
	}

	public static MulliganDoneMessage decode(final FriendlyByteBuf buffer) {
		return new MulliganDoneMessage(buffer.readUUID());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.mulliganDone(id);
	}
}
