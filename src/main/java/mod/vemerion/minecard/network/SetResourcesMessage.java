package mod.vemerion.minecard.network;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

public class SetResourcesMessage extends ServerToClientMessage {

	private UUID id;
	private int resources;
	private int maxResources;

	public SetResourcesMessage(UUID id, int resources, int maxResources) {
		this.id = id;
		this.resources = resources;
		this.maxResources = maxResources;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeInt(resources);
		buffer.writeInt(maxResources);
	}

	public static SetResourcesMessage decode(final FriendlyByteBuf buffer) {
		return new SetResourcesMessage(buffer.readUUID(), buffer.readInt(), buffer.readInt());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.setResources(id, resources, maxResources);
	}
}
