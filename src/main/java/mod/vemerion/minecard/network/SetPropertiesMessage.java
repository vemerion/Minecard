package mod.vemerion.minecard.network;

import java.util.Map;
import java.util.UUID;

import mod.vemerion.minecard.game.CardProperty;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class SetPropertiesMessage extends ServerToClientMessage {

	private UUID id;
	private int cardId;
	private Map<ResourceLocation, Integer> properties;

	public SetPropertiesMessage(UUID id, int cardId, Map<ResourceLocation, Integer> properties) {
		this.id = id;
		this.cardId = cardId;
		this.properties = properties;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeInt(cardId);
		MessageUtil.encode(buffer, properties, CardProperty.CODEC_MAP);
	}

	public static SetPropertiesMessage decode(final FriendlyByteBuf buffer) {
		return new SetPropertiesMessage(buffer.readUUID(), buffer.readInt(),
				MessageUtil.decode(buffer, CardProperty.CODEC_MAP));
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.setProperties(id, cardId, properties);
	}
}
