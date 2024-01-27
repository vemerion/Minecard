package mod.vemerion.minecard.network;

import java.util.List;

import mod.vemerion.minecard.game.Card;
import net.minecraft.network.FriendlyByteBuf;

public class PlayerChoiceMessage extends ServerToClientMessage {

	private String textKey;
	private List<Card> cards;
	private boolean targeting;

	public PlayerChoiceMessage(String textKey, List<Card> cards, boolean targeting) {
		this.textKey = textKey;
		this.cards = cards;
		this.targeting = targeting;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUtf(textKey);
		buffer.writeCollection(cards, (b, c) -> MessageUtil.encodeCard(b, c));
		buffer.writeBoolean(targeting);
	}

	public static PlayerChoiceMessage decode(final FriendlyByteBuf buffer) {
		return new PlayerChoiceMessage(buffer.readUtf(), buffer.readList(b -> MessageUtil.decodeCard(b)),
				buffer.readBoolean());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.playerChoice(new GameClient.Choice(textKey, cards, targeting));
	}
}
