package mod.vemerion.minecard.screen.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;

public class ParticlesAnimation extends Animation {
	public static record Color(float red, float green, float blue) {

	}

	public static record ParticleConfig(Color color, float minSize, float maxSize, List<ResourceLocation> textures) {
		public static final List<ResourceLocation> POTION_TEXTURES = IntStream.range(0, 8)
				.mapToObj(i -> new ResourceLocation("textures/particle/effect_" + String.valueOf(i) + ".png")).toList();
		public static final List<ResourceLocation> GLOW_TEXTURES = List
				.of(new ResourceLocation("textures/particle/glow.png"));
		public static final List<ResourceLocation> EXPLOSION_TEXTURES = IntStream.range(0, 16)
				.mapToObj(i -> new ResourceLocation("textures/particle/explosion_" + String.valueOf(i) + ".png"))
				.toList();
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
			p.render(source, partialTick);
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

		private static final float Z = 0.1f;

		private Vec2 pos;
		private float startSize;
		private int timer;
		private final int duration;
		private ParticleConfig config;

		private GameParticle(ParticleConfig config, Random rand, Vec2 pos) {
			this.config = config;
			this.pos = pos;
			this.startSize = rand.nextFloat(config.minSize, config.maxSize);
			this.duration = rand.nextInt(15, 30);
		}

		private void tick() {
			timer++;
		}

		private boolean isDone() {
			return timer >= duration;
		}

		private void render(BufferSource source, float partialTick) {
			var bufferbuilder = source
					.getBuffer(RenderType.text(config.textures.get((timer / 2) % config.textures.size())));

			float progress = (timer + partialTick) / duration;
			float size = Mth.lerp(progress, startSize, 0);
			float alpha = Mth.clampedLerp(0, 1, progress * 5);
			var red = config.color.red;
			var green = config.color.green;
			var blue = config.color.blue;
			bufferbuilder.vertex(pos.x, pos.y + size, Z).color(red, green, blue, alpha).uv(0, 1)
					.uv2(LightTexture.FULL_BRIGHT).endVertex();
			bufferbuilder.vertex(pos.x + size, pos.y + size, Z).color(red, green, blue, alpha).uv(1, 1)
					.uv2(LightTexture.FULL_BRIGHT).endVertex();
			bufferbuilder.vertex(pos.x + size, pos.y, Z).color(red, green, blue, alpha).uv(1, 0)
					.uv2(LightTexture.FULL_BRIGHT).endVertex();
			bufferbuilder.vertex(pos.x, pos.y, Z).color(red, green, blue, alpha).uv(0, 0).uv2(LightTexture.FULL_BRIGHT)
					.endVertex();
		}
	}
}
