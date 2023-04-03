package mod.vemerion.minecard.network;

import java.util.UUID;
import java.util.function.Supplier;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class CombatMessage {

	private UUID attackerId;
	private Card attacker;
	private UUID targetId;
	private Card target;

	public CombatMessage(UUID attackerId, Card attacker, UUID targetId, Card target) {
		this.attackerId = attackerId;
		this.attacker = attacker;
		this.targetId = targetId;
		this.target = target;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(attackerId);
		MessageUtil.encodeCard(buffer, attacker);
		buffer.writeUUID(targetId);
		MessageUtil.encodeCard(buffer, target);
	}

	public static CombatMessage decode(final FriendlyByteBuf buffer) {
		return new CombatMessage(buffer.readUUID(), MessageUtil.decodeCard(buffer), buffer.readUUID(), MessageUtil.decodeCard(buffer));
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
						game.combat(message.attackerId, message.attacker, message.targetId, message.target);
					}
				}
			};
		}
	}
}
