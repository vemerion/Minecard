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

public class SetResourcesMessage {

	private UUID id;
	private int resources;
	private int maxResources;

	public SetResourcesMessage(UUID id, int resources, int maxResources) {
		this.id = id;
		this.resources = resources;
		this.maxResources = maxResources;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeInt(resources);
		buffer.writeInt(maxResources);
	}

	public static SetResourcesMessage decode(final FriendlyByteBuf buffer) {
		return new SetResourcesMessage(buffer.readUUID(), buffer.readInt(), buffer.readInt());
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(this)));
	}

	private static class Handle {
		private static SafeRunnable handle(SetResourcesMessage message) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					var mc = Minecraft.getInstance();
					if (mc == null)
						return;

					if (mc.screen instanceof GameScreen game) {
						game.setResources(message.id, message.resources, message.maxResources);
					}
				}
			};
		}
	}
}