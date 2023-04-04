package mod.vemerion.minecard.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import mod.vemerion.minecard.game.CardType;
import mod.vemerion.minecard.game.Cards;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class UpdateCardTypesMessage {

	private Map<ResourceLocation, CardType> cardTypes;

	public UpdateCardTypesMessage(Map<ResourceLocation, CardType> cardTypes) {
		this.cardTypes = cardTypes;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeInt(cardTypes.size());
		for (var entry : cardTypes.entrySet()) {
			buffer.writeResourceLocation(entry.getKey());
			MessageUtil.encode(buffer, entry.getValue(), CardType.CODEC);
		}
	}

	public static UpdateCardTypesMessage decode(final FriendlyByteBuf buffer) {
		Map<ResourceLocation, CardType> cardTypes = new HashMap<>();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			var key = buffer.readResourceLocation();
			cardTypes.put(key, MessageUtil.decode(buffer, CardType.CODEC));
		}
		return new UpdateCardTypesMessage(cardTypes);
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(cardTypes)));
	}

	private static class Handle {
		private static SafeRunnable handle(Map<ResourceLocation, CardType> cardTypes) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					Cards.getInstance(true).addCardTypes(cardTypes);
				}
			};
		}
	}
}
