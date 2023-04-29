package mod.vemerion.minecard.network;

import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;

public class CombatMessage extends ServerToClientMessage {

	private UUID attackerId;
	private int attackerCardId;
	private UUID targetId;
	private int targetCardId;

	public CombatMessage(UUID attackerId, int attackerCardId, UUID targetId, int targetCardId) {
		this.attackerId = attackerId;
		this.attackerCardId = attackerCardId;
		this.targetId = targetId;
		this.targetCardId = targetCardId;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(attackerId);
		buffer.writeInt(attackerCardId);
		buffer.writeUUID(targetId);
		buffer.writeInt(targetCardId);
	}

	public static CombatMessage decode(final FriendlyByteBuf buffer) {
		return new CombatMessage(buffer.readUUID(), buffer.readInt(), buffer.readUUID(), buffer.readInt());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.combat(attackerId, attackerCardId, targetId, targetCardId);
	}
}
