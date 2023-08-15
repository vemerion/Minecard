package mod.vemerion.minecard.network;

import mod.vemerion.minecard.capability.PlayerStats;
import net.minecraft.network.FriendlyByteBuf;

public class StatMessage extends ServerToClientMessage {

	private PlayerStats.Key key;
	private int value;
	private String name;

	public StatMessage(PlayerStats.Key key, int value, String name) {
		this.key = key;
		this.value = value;
		this.name = name;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		MessageUtil.encode(buffer, key, PlayerStats.Key.CODEC);
		buffer.writeInt(value);
		buffer.writeUtf(name);
	}

	public static StatMessage decode(final FriendlyByteBuf buffer) {
		return new StatMessage(MessageUtil.decode(buffer, PlayerStats.Key.CODEC), buffer.readInt(), buffer.readUtf());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.stat(key, value, name);
	}
}
