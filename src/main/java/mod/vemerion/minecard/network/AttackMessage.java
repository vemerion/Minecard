package mod.vemerion.minecard.network;

import java.util.function.Supplier;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class AttackMessage {

	private BlockPos pos;
	int attacker;
	int target;

	public AttackMessage(BlockPos pos, int attacker, int target) {
		this.pos = pos;
		this.attacker = attacker;
		this.target = target;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeInt(attacker);
		buffer.writeInt(target);
	}

	public static AttackMessage decode(final FriendlyByteBuf buffer) {
		return new AttackMessage(buffer.readBlockPos(), buffer.readInt(), buffer.readInt());
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);

		var sender = context.getSender();
		var level = sender.getLevel();
		if (level.isLoaded(pos) && level.getBlockEntity(pos) instanceof GameBlockEntity game) {
			game.attack(sender, attacker, target);
		}
	}
}
