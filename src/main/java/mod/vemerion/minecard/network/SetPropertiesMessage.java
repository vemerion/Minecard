package mod.vemerion.minecard.network;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class SetPropertiesMessage {

	private UUID id;
	private int cardId;
	private Map<CardProperty, Integer> properties;

	public SetPropertiesMessage(UUID id, int cardId, Map<CardProperty, Integer> properties) {
		this.id = id;
		this.cardId = cardId;
		this.properties = properties;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeInt(cardId);
		MessageUtil.encode(buffer, properties, CardProperty.CODEC_MAP);
	}

	public static SetPropertiesMessage decode(final FriendlyByteBuf buffer) {
		return new SetPropertiesMessage(buffer.readUUID(), buffer.readInt(),
				MessageUtil.decode(buffer, CardProperty.CODEC_MAP));
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(this)));
	}

	private static class Handle {
		private static SafeRunnable handle(SetPropertiesMessage message) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					var mc = Minecraft.getInstance();
					if (mc == null)
						return;

					if (mc.screen instanceof GameScreen game) {
						game.setProperties(message.id, message.cardId, message.properties);
					}
				}
			};
		}
	}
}
