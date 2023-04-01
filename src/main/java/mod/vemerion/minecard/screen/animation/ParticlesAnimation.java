package mod.vemerion.minecard.screen.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;

public class ParticlesAnimation extends Animation {

	private List<GameParticle> particles = new ArrayList<>();
	private AABB area;
	private int timer;
	private int delay;
	private int count;

	public ParticlesAnimation(Minecraft mc, AABB area, int count, int delay, Runnable onDone) {
		super(mc, onDone);
		this.area = area;
		this.count = count;
		this.delay = delay;
	}

	private static Vec2 randomInAABB(Random rand, AABB area) {
		return new Vec2(rand.nextFloat((float) area.minX, (float) area.maxX),
				rand.nextFloat((float) area.minY, (float) area.maxY));
	}

	@Override
	public boolean isDone() {
		return particles.isEmpty() && timer > delay;
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		for (var p : particles)
			p.render(partialTick);
	}

	@Override
	public void tick() {
		timer++;

		if (timer <= delay) {
			var rand = mc.level.random;
			for (int i = 0; i < count; i++) {
				particles.add(new GameParticle(rand, randomInAABB(rand, area)));
			}
		}

		for (var p : particles)
			p.tick();

		for (int i = particles.size() - 1; i >= 0; i--)
			if (particles.get(i).isDone())
				particles.remove(i);
	}

	private static class GameParticle {

		private static final ResourceLocation[] TEXTURES = IntStream.range(0, 8)
				.mapToObj(i -> new ResourceLocation("textures/particle/effect_" + String.valueOf(i) + ".png"))
				.toArray(ResourceLocation[]::new);

		private Vec2 pos;
		private float red;
		private float green;
		private float blue;
		private float alpha;
		private float startSize;
		private int timer;
		private final int duration;

		private GameParticle(Random rand, Vec2 pos) {
			this.pos = pos;
			this.red = rand.nextFloat(0.5f, 1);
			this.green = rand.nextFloat(0, 0.2f);
			this.blue = rand.nextFloat(0, 0.2f);
			this.alpha = 1;
			this.startSize = rand.nextInt(15, 25);
			this.duration = rand.nextInt(15, 30);
		}

		private void tick() {
			timer++;
		}

		private boolean isDone() {
			return timer >= duration;
		}

		private void render(float partialTick) {
			Tesselator tesselator = Tesselator.getInstance();
			BufferBuilder bufferbuilder = tesselator.getBuilder();
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			RenderSystem.setShaderTexture(0, TEXTURES[(timer / 2) % TEXTURES.length]);
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.setShaderColor(1, 1, 1, 1);

			float progress = (timer + partialTick) / duration;
			float size = Mth.lerp(progress, startSize, 0);
			alpha = Mth.clampedLerp(0, 1, progress * 5);
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferbuilder.vertex(pos.x, pos.y + size, 0).uv(0, 1).color(red, green, blue, alpha).endVertex();
			bufferbuilder.vertex(pos.x + size, pos.y + size, 0).uv(1, 1).color(red, green, blue, alpha).endVertex();
			bufferbuilder.vertex(pos.x + size, pos.y, 0).uv(1, 0).color(red, green, blue, alpha).endVertex();
			bufferbuilder.vertex(pos.x, pos.y, 0).uv(0, 0).color(red, green, blue, alpha).endVertex();
			tesselator.end();
		}
	}
}
