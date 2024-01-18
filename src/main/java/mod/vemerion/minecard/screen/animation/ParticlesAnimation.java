package mod.vemerion.minecard.screen.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
		public static final Codec<Color> CODEC = RecordCodecBuilder
				.create(instance -> instance.group(Codec.FLOAT.fieldOf("red").forGetter(Color::red),
						Codec.FLOAT.fieldOf("green").forGetter(Color::green),
						Codec.FLOAT.fieldOf("blue").forGetter(Color::blue)).apply(instance, Color::new));
	}

	public static record ParticleConfig(Color color, float minSize, float maxSize, float minSpeed, float maxSpeed,
			List<ResourceLocation> textures) {
		public static final List<ResourceLocation> POTION_TEXTURES = IntStream.range(0, 8)
				.mapToObj(i -> new ResourceLocation("textures/particle/effect_" + String.valueOf(i) + ".png")).toList();
		public static final List<ResourceLocation> GLOW_TEXTURES = List
				.of(new ResourceLocation("textures/particle/glow.png"));
		public static final List<ResourceLocation> EXPLOSION_TEXTURES = IntStream.range(0, 16)
				.mapToObj(i -> new ResourceLocation("textures/particle/explosion_" + String.valueOf(i) + ".png"))
				.toList();
		public static final List<ResourceLocation> GENERIC_REVERSE_TEXTURES = IntStream.of(7, 6, 5, 4, 3, 2, 1, 0)
				.mapToObj(i -> new ResourceLocation("textures/particle/generic_" + String.valueOf(i) + ".png"))
				.toList();
		public static final List<ResourceLocation> SONIC_BOOM_TEXTURES = IntStream.range(0, 16)
				.mapToObj(i -> new ResourceLocation("textures/particle/sonic_boom_" + String.valueOf(i) + ".png"))
				.toList();
	}

	private List<GameParticle> particles = new ArrayList<>();
	private Supplier<Vec2> generatePos;
	private int timer;
	private int delay;
	private int count;
	private ParticleConfig config;
	private Random rand = new Random();

	public ParticlesAnimation(Minecraft mc, Supplier<Vec2> generatePos, int count, int delay, ParticleConfig config,
			Runnable onDone) {
		super(mc, onDone);
		this.generatePos = generatePos;
		this.count = count;
		this.delay = delay;
		this.config = config;
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
			for (int i = 0; i < count; i++) {
				particles.add(new GameParticle(config, rand, generatePos.get()));
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

		private Vec2 position;
		private float startSize;
		private float speed;
		private Vec2 direction;
		private int timer;
		private final int duration;
		private ParticleConfig config;

		private GameParticle(ParticleConfig config, Random rand, Vec2 position) {
			this.config = config;
			this.position = position;
			this.startSize = rand.nextFloat(config.minSize, config.maxSize);
			this.speed = config.maxSpeed == 0 ? 0 : rand.nextFloat(config.minSpeed, config.maxSpeed);
			this.direction = new Vec2(rand.nextFloat(-1, 1), rand.nextFloat(-1, 1)).normalized();
			this.duration = rand.nextInt(15, 30);
		}

		private void tick() {
			timer++;
		}

		private boolean isDone() {
			return timer >= duration;
		}

		private void render(BufferSource source, float partialTick) {
			var bufferbuilder = source.getBuffer(
					RenderType.text(config.textures.get((int) (((float) timer / duration) * config.textures.size()))));

			float progress = (timer + partialTick) / duration;
			float size = Mth.lerp(progress, startSize, 0);
			float alpha = Mth.clampedLerp(0, 1, progress * 5);
			var red = config.color.red;
			var green = config.color.green;
			var blue = config.color.blue;
			var pos = position.add(direction.scale(timer + partialTick).scale(speed)).add(-size / 2);
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
