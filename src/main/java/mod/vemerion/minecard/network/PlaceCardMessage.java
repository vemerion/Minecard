package mod.vemerion.minecard.network;

import java.util.UUID;

import mod.vemerion.minecard.game.Card;
import net.minecraft.network.FriendlyByteBuf;

public class PlaceCardMessage extends ServerToClientMessage {

	private UUID id;
	private Card card;
	private int leftId;

	public PlaceCardMessage(UUID id, Card card, int leftId) {
		this.id = id;
		this.card = card;
		this.leftId = leftId;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		MessageUtil.encodeCard(buffer, card);
		buffer.writeInt(leftId);
	}

	public static PlaceCardMessage decode(final FriendlyByteBuf buffer) {
		return new PlaceCardMessage(buffer.readUUID(), MessageUtil.decodeCard(buffer), buffer.readInt());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.placeCard(id, card, leftId);
	}
}
