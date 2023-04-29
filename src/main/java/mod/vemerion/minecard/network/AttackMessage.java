package mod.vemerion.minecard.network;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class AttackMessage extends ClientToServerMessage {

	private int attacker;
	private int target;

	public AttackMessage(BlockPos pos, int attacker, int target) {
		super(pos);
		this.attacker = attacker;
		this.target = target;
	}

	@Override
	protected void encodeAdditional(FriendlyByteBuf buffer) {
		buffer.writeInt(attacker);
		buffer.writeInt(target);
	}

	public static AttackMessage decode(final FriendlyByteBuf buffer) {
		return new AttackMessage(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
	}

	@Override
	protected void handle(GameBlockEntity game, ServerPlayer sender) {
		game.attack(attacker, target);
	}
}
