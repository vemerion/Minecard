package mod.vemerion.minecard.network;

import java.util.List;
import java.util.function.Supplier;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class UpdateCardsMessage {

	private List<Card> cards;

	public UpdateCardsMessage(List<Card> cards) {
		this.cards = cards;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeCollection(cards, MessageUtil::encodeCard);
	}

	public static UpdateCardsMessage decode(final FriendlyByteBuf buffer) {
		return new UpdateCardsMessage(buffer.readList(MessageUtil::decodeCard));
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(this)));
	}

	private static class Handle {
		private static SafeRunnable handle(UpdateCardsMessage message) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					var mc = Minecraft.getInstance();
					if (mc == null)
						return;

					if (mc.screen instanceof GameScreen game) {
						for (var card : message.cards)
							game.updateCard(card);
					}
				}
			};
		}
	}
}
