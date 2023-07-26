package mod.vemerion.minecard.network;

import java.util.List;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.ability.CardAbility;
import net.minecraft.network.FriendlyByteBuf;

public class PlayerChoiceMessage extends ServerToClientMessage {

	private CardAbility ability;
	private List<Card> cards;
	private boolean targeting;

	public PlayerChoiceMessage(CardAbility ability, List<Card> cards, boolean targeting) {
		this.ability = ability;
		this.cards = cards;
		this.targeting = targeting;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		MessageUtil.encode(buffer, ability, CardAbility.CODEC);
		buffer.writeCollection(cards, (b, c) -> MessageUtil.encodeCard(b, c));
		buffer.writeBoolean(targeting);
	}

	public static PlayerChoiceMessage decode(final FriendlyByteBuf buffer) {
		return new PlayerChoiceMessage(MessageUtil.decode(buffer, CardAbility.CODEC),
				buffer.readList(b -> MessageUtil.decodeCard(b)), buffer.readBoolean());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.playerChoice(new GameClient.Choice(ability, cards, targeting));
	}
}
