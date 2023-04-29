package mod.vemerion.minecard.network;

import java.util.List;
import java.util.UUID;

import mod.vemerion.minecard.game.Card;
import net.minecraft.network.FriendlyByteBuf;

public class DrawCardsMessage extends ServerToClientMessage {

	private UUID id;
	private List<Card> cards;
	private boolean shrinkDeck;

	public DrawCardsMessage(UUID id, List<Card> cards, boolean shrinkDeck) {
		this.id = id;
		this.cards = cards;
		this.shrinkDeck = shrinkDeck;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeCollection(cards, MessageUtil::encodeCard);
		buffer.writeBoolean(shrinkDeck);
	}

	public static DrawCardsMessage decode(final FriendlyByteBuf buffer) {
		return new DrawCardsMessage(buffer.readUUID(), buffer.readList(MessageUtil::decodeCard), buffer.readBoolean());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.drawCards(id, cards, shrinkDeck);
	}
}
