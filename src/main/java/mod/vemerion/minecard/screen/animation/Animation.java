package mod.vemerion.minecard.screen.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

public abstract class Animation {

	protected Minecraft mc;
	private Runnable runnable;

	public Animation(Minecraft mc, Runnable onDone) {
		this.mc = mc;
		this.runnable = onDone;
	}

	public void tick() {

	}

	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {

	}

	public abstract boolean isDone();

	public void onDone() {
		runnable.run();
	}
}
