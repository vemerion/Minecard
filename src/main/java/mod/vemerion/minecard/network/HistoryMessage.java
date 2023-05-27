package mod.vemerion.minecard.network;

import mod.vemerion.minecard.game.HistoryEntry;
import net.minecraft.network.FriendlyByteBuf;

public class HistoryMessage extends ServerToClientMessage {

	private HistoryEntry entry;

	public HistoryMessage(HistoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		MessageUtil.encode(buffer, entry, HistoryEntry.CODEC);
	}

	public static HistoryMessage decode(final FriendlyByteBuf buffer) {
		return new HistoryMessage(MessageUtil.decode(buffer, HistoryEntry.CODEC));
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.history(entry);
	}
}
