package mod.vemerion.minecard.network;

import java.util.UUID;
import java.util.function.Supplier;

import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class CombatMessage {

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

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(attackerId);
		buffer.writeInt(attackerCardId);
		buffer.writeUUID(targetId);
		buffer.writeInt(targetCardId);
	}

	public static CombatMessage decode(final FriendlyByteBuf buffer) {
		return new CombatMessage(buffer.readUUID(), buffer.readInt(), buffer.readUUID(), buffer.readInt());
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(this)));
	}

	private static class Handle {
		private static SafeRunnable handle(CombatMessage message) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					var mc = Minecraft.getInstance();
					if (mc == null)
						return;

					if (mc.screen instanceof GameScreen game) {
						game.combat(message.attackerId, message.attackerCardId, message.targetId, message.targetCardId);
					}
				}
			};
		}
	}
}
