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

public class PlaceCardMessage {

	private UUID id;
	private Card card;
	private int leftId;

	public PlaceCardMessage(UUID id, Card card, int leftId) {
		this.id = id;
		this.card = card;
		this.leftId = leftId;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		MessageUtil.encodeCard(buffer, card);
		buffer.writeInt(leftId);
	}

	public static PlaceCardMessage decode(final FriendlyByteBuf buffer) {
		return new PlaceCardMessage(buffer.readUUID(), MessageUtil.decodeCard(buffer), buffer.readInt());
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(this)));
	}

	private static class Handle {
		private static SafeRunnable handle(PlaceCardMessage message) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					var mc = Minecraft.getInstance();
					if (mc == null)
						return;

					if (mc.screen instanceof GameScreen game) {
						game.placeCard(message.id, message.card, message.leftId);
					}
				}
			};
		}
	}
}
