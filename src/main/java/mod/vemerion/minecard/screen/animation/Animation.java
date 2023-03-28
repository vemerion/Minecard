package mod.vemerion.minecard.screen.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;

public abstract class Animation {
	protected Minecraft mc;

	public Animation(Minecraft mc) {
		this.mc = mc;
	}

	public void tick() {

	}

	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {

	}
	
	public abstract boolean isDone();
}
