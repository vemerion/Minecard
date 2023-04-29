package mod.vemerion.minecard.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.MessagePlayerState;
import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class OpenGameMessage extends ServerToClientMessage {

	private List<MessagePlayerState> state;
	private BlockPos pos;

	public OpenGameMessage(List<MessagePlayerState> state, BlockPos pos) {
		this.state = state;
		this.pos = pos;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		for (var player : state)
			writePlayer(buffer, player);
		buffer.writeBlockPos(pos);
	}

	private static void writePlayer(final FriendlyByteBuf buffer, MessagePlayerState player) {
		buffer.writeUUID(player.id);
		buffer.writeInt(player.deck);
		writeCards(buffer, player.hand);
		writeCards(buffer, player.board);
		buffer.writeInt(player.resources);
		buffer.writeInt(player.maxResources);
	}

	private static MessagePlayerState readPlayer(final FriendlyByteBuf buffer) {
		return new MessagePlayerState(buffer.readUUID(), buffer.readInt(), readCards(buffer), readCards(buffer),
				buffer.readInt(), buffer.readInt());
	}

	private static void writeCards(final FriendlyByteBuf buffer, List<Card> cards) {
		buffer.writeInt(cards.size());
		for (var card : cards) {
			MessageUtil.encodeCard(buffer, card);
		}
	}

	private static List<Card> readCards(final FriendlyByteBuf buffer) {
		List<Card> result = new ArrayList<>();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			result.add(MessageUtil.decodeCard(buffer));
		}
		return result;
	}

	public static OpenGameMessage decode(final FriendlyByteBuf buffer) {
		return new OpenGameMessage(List.of(readPlayer(buffer), readPlayer(buffer)), buffer.readBlockPos());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.openGame(state, pos);
	}

	@Override
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
