package mod.vemerion.minecard.network;

import java.util.List;

import mod.vemerion.minecard.game.Card;
import net.minecraft.network.FriendlyByteBuf;

public class UpdateCardsMessage extends ServerToClientMessage {

	private List<Card> cards;

	public UpdateCardsMessage(List<Card> cards) {
		this.cards = cards;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeCollection(cards, MessageUtil::encodeCard);
	}

	public static UpdateCardsMessage decode(final FriendlyByteBuf buffer) {
		return new UpdateCardsMessage(buffer.readList(MessageUtil::decodeCard));
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		for (var card : cards)
			client.updateCard(card);
	}
}
