package mod.vemerion.minecard.network;

import java.util.List;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class AnimationMessage extends ServerToClientMessage {

	private int originId;
	private List<Integer> targets;
	private ResourceLocation animation;

	public AnimationMessage(int originId, List<Integer> targets, ResourceLocation animation) {
		this.originId = originId;
		this.targets = targets;
		this.animation = animation;
	}

	@Override
	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeInt(originId);
		buffer.writeCollection(targets, (b, id) -> b.writeInt(id));
		buffer.writeResourceLocation(animation);
	}

	public static AnimationMessage decode(final FriendlyByteBuf buffer) {
		return new AnimationMessage(buffer.readInt(), buffer.readList(FriendlyByteBuf::readInt),
				buffer.readResourceLocation());
	}

	@Override
	public ServerToClientMessage create(FriendlyByteBuf buffer) {
		return decode(buffer);
	}

	@Override
	public void handle(GameClient client) {
		client.animation(originId, targets, animation);
	}
}
