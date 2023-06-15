package mod.vemerion.minecard.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import mod.vemerion.minecard.game.CardProperties;
import mod.vemerion.minecard.game.CardProperty;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class UpdateCardPropertiesMessage {

	private Map<ResourceLocation, CardProperty> cardProperties;

	public UpdateCardPropertiesMessage(Map<ResourceLocation, CardProperty> cardProperties) {
		this.cardProperties = cardProperties;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeInt(cardProperties.size());
		for (var entry : cardProperties.entrySet()) {
			buffer.writeResourceLocation(entry.getKey());
			MessageUtil.encode(buffer, entry.getValue(), CardProperty.CODEC);
		}
	}

	public static UpdateCardPropertiesMessage decode(final FriendlyByteBuf buffer) {
		Map<ResourceLocation, CardProperty> cardProperties = new HashMap<>();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			var key = buffer.readResourceLocation();
			cardProperties.put(key, MessageUtil.decode(buffer, CardProperty.CODEC));
		}
		return new UpdateCardPropertiesMessage(cardProperties);
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(cardProperties)));
	}

	private static class Handle {
		private static SafeRunnable handle(Map<ResourceLocation, CardProperty> cardProperties) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					CardProperties.getInstance(true).addProperties(cardProperties);
				}
			};
		}
	}
}
