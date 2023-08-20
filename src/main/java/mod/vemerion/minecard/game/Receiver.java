package mod.vemerion.minecard.game;

import java.util.UUID;

import io.netty.buffer.Unpooled;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.ServerToClientMessage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public abstract class Receiver {

	public abstract UUID getId();

	public abstract void receiver(ServerToClientMessage message);

	public static class AI extends Receiver {
		private AIPlayer ai;

		public AI(AIPlayer ai) {
			this.ai = ai;
		}

		@Override
		public UUID getId() {
			return ai.getId();
		}

		@Override
		public void receiver(ServerToClientMessage message) {
			var buffer = new FriendlyByteBuf(Unpooled.buffer());
			message.encode(buffer);
			message.create(buffer).handle(ai);
		}
	}

	public static class Player extends Receiver {
		private ServerPlayer player;

		public Player(ServerPlayer player) {
			this.player = player;
		}

		@Override
		public UUID getId() {
			return player.getUUID();
		}

		@Override
		public void receiver(ServerToClientMessage message) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
		}
	}
}
