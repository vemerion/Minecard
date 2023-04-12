package mod.vemerion.minecard.network;

import java.util.List;
import java.util.function.Supplier;

import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.DistExecutor.SafeRunnable;
import net.minecraftforge.network.NetworkEvent;

public class AnimationMessage {

	private int originId;
	private List<Integer> targets;
	private ResourceLocation animation;

	public AnimationMessage(int originId, List<Integer> targets, ResourceLocation animation) {
		this.originId = originId;
		this.targets = targets;
		this.animation = animation;
	}

	public void encode(final FriendlyByteBuf buffer) {
		buffer.writeInt(originId);
		buffer.writeCollection(targets, (b, id) -> b.writeInt(id));
		buffer.writeResourceLocation(animation);
	}

	public static AnimationMessage decode(final FriendlyByteBuf buffer) {
		return new AnimationMessage(buffer.readInt(), buffer.readList(FriendlyByteBuf::readInt),
				buffer.readResourceLocation());
	}

	public void handle(final Supplier<NetworkEvent.Context> supplier) {
		final NetworkEvent.Context context = supplier.get();
		context.setPacketHandled(true);
		context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> Handle.handle(this)));
	}

	private static class Handle {
		private static SafeRunnable handle(AnimationMessage message) {
			return new SafeRunnable() {
				private static final long serialVersionUID = 1L;

				@Override
				public void run() {
					var mc = Minecraft.getInstance();
					if (mc == null)
						return;

					if (mc.screen instanceof GameScreen game) {
						game.animation(message.originId, message.targets, message.animation);
					}
				}
			};
		}
	}
}
