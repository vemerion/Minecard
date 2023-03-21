package mod.vemerion.minecard.network;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class SetReadyMessage {

	private UUID id;
	private List<Integer> cards;

	public SetReadyMessage(UUID id, List<Integer> cards) {
		this.id = id;
		this.cards = cards;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeCollection(cards, (b, c) -> b.writeInt(c));
	}

	public static SetReadyMessage decode(final FriendlyByteBuf buffer) {
		return new SetReadyMessage(buffer.readUUID(), buffer.readList(b -> b.readInt()));
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(this)));
	}

	private static class Handle {
		private static SafeRunnable handle(SetReadyMessage message) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					var mc = Minecraft.getInstance();
					if (mc == null)
						return;

					if (mc.screen instanceof GameScreen game) {
						game.setReady(message.id, message.cards);
					}
				}
			};
		}
	}
}
