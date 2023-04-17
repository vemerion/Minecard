package mod.vemerion.minecard.network;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class UpdateDecksMessage {

	private Map<UUID, Integer> sizes;

	public UpdateDecksMessage(Map<UUID, Integer> sizes) {
		this.sizes = sizes;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeMap(sizes, FriendlyByteBuf::writeUUID, FriendlyByteBuf::writeInt);
	}

	public static UpdateDecksMessage decode(final FriendlyByteBuf buffer) {
		return new UpdateDecksMessage(buffer.readMap(FriendlyByteBuf::readUUID, FriendlyByteBuf::readInt));
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(this)));
	}

	private static class Handle {
		private static SafeRunnable handle(UpdateDecksMessage message) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					var mc = Minecraft.getInstance();
					if (mc == null)
						return;

					if (mc.screen instanceof GameScreen game) {
						game.updateDecks(message.sizes);
					}
				}
			};
		}
	}
}
