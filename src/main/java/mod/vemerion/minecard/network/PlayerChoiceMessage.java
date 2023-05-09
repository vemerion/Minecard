package mod.vemerion.minecard.network;

import java.util.List;

import mod.vemerion.minecard.game.ability.CardAbility;
import net.minecraft.network.FriendlyByteBuf;

public class PlayerChoiceMessage extends ServerToClientMessage {

	private int id;
	private CardAbility ability;
	private List<Integer> cards;

	public PlayerChoiceMessage(int id, CardAbility ability, List<Integer> cards) {
		this.id = id;
		this.ability = ability;
		this.cards = cards;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeInt(id);
		MessageUtil.encode(buffer, ability, CardAbility.CODEC);
		buffer.writeCollection(cards, (b, i) -> b.writeInt(i));
	}

	public static PlayerChoiceMessage decode(final FriendlyByteBuf buffer) {
		return new PlayerChoiceMessage(buffer.readInt(), MessageUtil.decode(buffer, CardAbility.CODEC),
				buffer.readList(b -> b.readInt()));
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.playerChoice(new GameClient.Choice(id, ability, cards));
	}
}
