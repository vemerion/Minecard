package mod.vemerion.minecard.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.ClientPlayerState;
import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class OpenGameMessage {

	private List<ClientPlayerState> state;
	private BlockPos pos;

	public OpenGameMessage(List<ClientPlayerState> state, BlockPos pos) {
		this.state = state;
		this.pos = pos;
	}

	public void encode(final FriendlyByteBuf buffer) {
		for (var player : state)
			writePlayer(buffer, player);
		buffer.writeBlockPos(pos);
	}

	private static void writePlayer(final FriendlyByteBuf buffer, ClientPlayerState player) {
		buffer.writeUUID(player.id);
		buffer.writeInt(player.deck);
		writeCards(buffer, player.hand);
		writeCards(buffer, player.board);
	}

	private static ClientPlayerState readPlayer(final FriendlyByteBuf buffer) {
		return new ClientPlayerState(buffer.readUUID(), buffer.readInt(), readCards(buffer), readCards(buffer));
	}

	private static void writeCards(final FriendlyByteBuf buffer, List<Card> cards) {
		buffer.writeInt(cards.size());
		for (var card : cards) {
			CompoundTag tag = new CompoundTag();
			if (card.getType() != null)
				tag.put("value",
						Card.CODEC.encodeStart(NbtOps.INSTANCE, card).getOrThrow(false, OpenGameMessage::onError));
			buffer.writeNbt(tag);
		}
	}

	private static List<Card> readCards(final FriendlyByteBuf buffer) {
		List<Card> result = new ArrayList<>();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			var nbt = buffer.readNbt();
			if (nbt.contains("value"))
				result.add(Card.CODEC.parse(NbtOps.INSTANCE, nbt.get("value")).getOrThrow(false,
						OpenGameMessage::onError));
			else
				result.add(Cards.EMPTY);
		}
		return result;
	}

	private static void onError(String msg) {
	}

	public static OpenGameMessage decode(final FriendlyByteBuf buffer) {
		return new OpenGameMessage(List.of(readPlayer(buffer), readPlayer(buffer)), buffer.readBlockPos());
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

					mc.setScreen(new GameScreen(message.state, message.pos));
				}
			};
		}
	}
}
