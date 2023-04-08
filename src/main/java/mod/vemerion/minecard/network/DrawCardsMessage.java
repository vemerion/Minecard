package mod.vemerion.minecard.network;

import java.util.List;
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

public class DrawCardsMessage {

	private UUID id;
	private List<Card> cards;
	private boolean shrinkDeck;

	public DrawCardsMessage(UUID id, List<Card> cards, boolean shrinkDeck) {
		this.id = id;
		this.cards = cards;
		this.shrinkDeck = shrinkDeck;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeUUID(id);
		buffer.writeCollection(cards, MessageUtil::encodeCard);
		buffer.writeBoolean(shrinkDeck);
	}

	public static DrawCardsMessage decode(final FriendlyByteBuf buffer) {
		return new DrawCardsMessage(buffer.readUUID(), buffer.readList(MessageUtil::decodeCard), buffer.readBoolean());
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(this)));
	}

	private static class Handle {
		private static SafeRunnable handle(DrawCardsMessage message) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					var mc = Minecraft.getInstance();
					if (mc == null)
						return;

					if (mc.screen instanceof GameScreen game) {
						game.drawCards(message.id, message.cards, message.shrinkDeck);
					}
				}
			};
		}
	}
}
