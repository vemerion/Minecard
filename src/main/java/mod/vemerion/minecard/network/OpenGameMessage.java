package mod.vemerion.minecard.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.ClientState;
import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class OpenGameMessage {

	private ClientState state;

	public OpenGameMessage(ClientState state) {
		this.state = state;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeInt(state.enemyDeck);
		buffer.writeInt(state.yourDeck);
		buffer.writeInt(state.enemyHand);
		writeCards(buffer, state.yourHand);
		writeCards(buffer, state.enemyBoard);
		writeCards(buffer, state.yourBoard);
	}

	private static void writeCards(final FriendlyByteBuf buffer, List<Card> cards) {
		buffer.writeInt(cards.size());
		for (var card : cards) {
			CompoundTag tag = new CompoundTag();
			tag.put("value", Card.CODEC.encodeStart(NbtOps.INSTANCE, card).getOrThrow(false, OpenGameMessage::onError));
			buffer.writeNbt(tag);
		}
	}

	private static List<Card> readCards(final FriendlyByteBuf buffer) {
		List<Card> result = new ArrayList<>();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			result.add(Card.CODEC.parse(NbtOps.INSTANCE, buffer.readNbt().get("value")).getOrThrow(false,
					OpenGameMessage::onError));
		}
		return result;
	}

	private static void onError(String msg) {
	}

	public static OpenGameMessage decode(final FriendlyByteBuf buffer) {
		return new OpenGameMessage(new ClientState(buffer.readInt(), buffer.readInt(), buffer.readInt(),
				readCards(buffer), readCards(buffer), readCards(buffer)));
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(this)));
	}

	private static class Handle {
		private static SafeRunnable handle(OpenGameMessage message) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					var mc = Minecraft.getInstance();
					if (mc == null)
						return;

					mc.setScreen(new GameScreen(message.state));
				}
			};
		}
	}
}
