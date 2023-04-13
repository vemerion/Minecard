package mod.vemerion.minecard.screen.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
	public static record Color(float red, float green, float blue) {

	}

	public static record ParticleConfig(Color color, List<ResourceLocation> textures) {
		public static final List<ResourceLocation> POTION_TEXTURES = IntStream.range(0, 8)
				.mapToObj(i -> new ResourceLocation("textures/particle/effect_" + String.valueOf(i) + ".png")).toList();
		public static final List<ResourceLocation> GLOW_TEXTURES = List
				.of(new ResourceLocation("textures/particle/glow.png"));

	}

	private List<GameParticle> particles = new ArrayList<>();
	private AABB area;
	private int timer;
	private int delay;
	private int count;
	private ParticleConfig config;

	public ParticlesAnimation(Minecraft mc, AABB area, int count, int delay, ParticleConfig config, Runnable onDone) {
		super(mc, onDone);
		this.area = area;
		this.count = count;
		this.delay = delay;
		this.config = config;
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
				particles.add(new GameParticle(config, rand, randomInAABB(rand, area)));
			}
		}

		for (var p : particles)
			p.tick();

		for (int i = particles.size() - 1; i >= 0; i--)
			if (particles.get(i).isDone())
				particles.remove(i);
	}

	private static class GameParticle {
		private Vec2 pos;
		private float startSize;
		private int timer;
		private final int duration;
		private ParticleConfig config;

		private GameParticle(ParticleConfig config, Random rand, Vec2 pos) {
			this.config = config;
			this.pos = pos;
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
			var tesselator = Tesselator.getInstance();
			var bufferbuilder = tesselator.getBuilder();
			RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
			RenderSystem.setShaderTexture(0, config.textures.get((timer / 2) % config.textures.size()));
			RenderSystem.enableBlend();
			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			RenderSystem.setShaderColor(1, 1, 1, 1);

			float progress = (timer + partialTick) / duration;
			float size = Mth.lerp(progress, startSize, 0);
			float alpha = Mth.clampedLerp(0, 1, progress * 5);
			var red = config.color.red;
			var green = config.color.green;
			var blue = config.color.blue;
			bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferbuilder.vertex(pos.x, pos.y + size, 0).uv(0, 1).color(red, green, blue, alpha).endVertex();
			bufferbuilder.vertex(pos.x + size, pos.y + size, 0).uv(1, 1).color(red, green, blue, alpha).endVertex();
			bufferbuilder.vertex(pos.x + size, pos.y, 0).uv(1, 0).color(red, green, blue, alpha).endVertex();
			bufferbuilder.vertex(pos.x, pos.y, 0).uv(0, 0).color(red, green, blue, alpha).endVertex();
			tesselator.end();
		}
	}
}
