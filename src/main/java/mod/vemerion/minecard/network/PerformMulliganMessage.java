package mod.vemerion.minecard.network;

import java.util.HashSet;
import java.util.Set;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class PerformMulliganMessage extends ClientToServerMessage {

	private Set<Integer> cards;

	public PerformMulliganMessage(BlockPos pos, Set<Integer> cards) {
		super(pos);
		this.cards = cards;
	}

	@Override
	protected void encodeAdditional(FriendlyByteBuf buffer) {
		buffer.writeCollection(cards, (b, c) -> b.writeInt(c));
	}

	public static PerformMulliganMessage decode(final FriendlyByteBuf buffer) {
		return new PerformMulliganMessage(buffer.readBlockPos(),
				buffer.readCollection(i -> new HashSet<>(), b -> b.readInt()));
	}

	@Override
	protected boolean canAlwaysBeReceived() {
		return true;
	}

	@Override
	protected void handle(GameBlockEntity game, ServerPlayer sender) {
		game.performMulligan(sender.getUUID(), cards);
	}
}
