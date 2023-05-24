package mod.vemerion.minecard.network;

import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class SetTutorialStepMessage extends ClientToServerMessage {

	private int step;

	public SetTutorialStepMessage(BlockPos pos, int step) {
		super(pos);
		this.step = step;
	}

	@Override
	protected void encodeAdditional(FriendlyByteBuf buffer) {
		buffer.writeInt(step);
	}

	public static SetTutorialStepMessage decode(final FriendlyByteBuf buffer) {
		return new SetTutorialStepMessage(buffer.readBlockPos(), buffer.readInt());
	}

	@Override
	protected void handle(GameBlockEntity game, ServerPlayer sender) {
		game.setTutorialStep(sender, step);
	}

	@Override
	protected boolean canAlwaysBeReceived() {
		return true;
	}

}
