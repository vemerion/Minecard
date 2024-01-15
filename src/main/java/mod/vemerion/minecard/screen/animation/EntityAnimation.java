package mod.vemerion.minecard.screen.animation;

import java.util.Optional;
import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;

import mod.vemerion.minecard.game.GameUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.util.TransformationHelper;

public class EntityAnimation extends Animation {

	public static enum SpecialAnimation {
		NONE("none"), EVOKER_FANGS_EAT("evoker_fangs_eat"), FROG_TONGUE("frog_tongue"), WARDEN_SPAWN("warden_spawn");

		public static final Codec<SpecialAnimation> CODEC = GameUtil.enumCodec(SpecialAnimation.class,
				SpecialAnimation::getName);

		private String name;

		private SpecialAnimation(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	private final Vec2 start;
	private Supplier<Vec2> target;
	private int timer;
	private Entity entity;
	private final int duration;
	private final int size;
	private final int soundDelay;
	private Optional<SoundEvent> startSound;
	private Optional<SoundEvent> durationSound;
	private Optional<SoundEvent> impactSound;
	private SpecialAnimation specialAnimation;

	public EntityAnimation(Minecraft mc, Vec2 start, Supplier<Vec2> target, EntityType<?> type, int duration, int size,
			int soundDelay, Optional<SoundEvent> startSound, Optional<SoundEvent> durationSound,
			Optional<SoundEvent> impactSound, SpecialAnimation specialAnimation, Runnable onDone) {
		super(mc, onDone);
		this.start = start;
		this.target = target;
		this.duration = duration;
		this.size = size;
		this.soundDelay = soundDelay;
		this.startSound = startSound;
		this.durationSound = durationSound;
		this.impactSound = impactSound;
		this.specialAnimation = specialAnimation;
		this.entity = create(type);
		if (specialAnimation == SpecialAnimation.FROG_TONGUE && entity instanceof Frog frog) {
			frog.tongueAnimationState.start(frog.tickCount);
		} else if (specialAnimation == SpecialAnimation.WARDEN_SPAWN && entity instanceof Warden warden) {
			warden.emergeAnimationState.start(warden.tickCount);
			warden.yBodyRotO = 90;
			warden.yBodyRot = 90;
			warden.yHeadRotO = 90;
			warden.yHeadRot = 90;
		}
	}

	private Entity create(EntityType<?> type) {
		if (specialAnimation == SpecialAnimation.EVOKER_FANGS_EAT && type == EntityType.EVOKER_FANGS) {
			return new EvokerFangs(EntityType.EVOKER_FANGS, mc.level) {
				@Override
				public float getAnimationProgress(float pPartialTicks) {
					return (timer + pPartialTicks) / duration;
				};
			};
		}
		return type.create(mc.level);
	}

	@Override
	public boolean isDone() {
		return timer >= duration;
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var poseStack = new PoseStack();
		float progress = (timer + partialTick) / duration;
		var targetPos = target.get();
		var pos = start == null ? targetPos
				: new Vec2(Mth.lerp(progress, start.x, targetPos.x), Mth.lerp(progress, start.y, targetPos.y));
		poseStack.translate(pos.x, pos.y, 0);
		poseStack.scale(size * (entity instanceof EnderDragon ? -1 : 1), -size, size);
		poseStack.mulPose(TransformationHelper.quatFromXYZ(Mth.HALF_PI,
				(float) -Mth.atan2(targetPos.y - pos.y, targetPos.x - pos.x) + Mth.HALF_PI, -0.6f, false));

		mc.getEntityRenderDispatcher().getRenderer(entity).render(entity, 0, partialTick, poseStack, source,
				LightTexture.FULL_BRIGHT);
	}

	@Override
	public void tick() {
		timer++;
		entity.tickCount++;
		if (timer == soundDelay && startSound.isPresent())
			mc.getSoundManager().play(SimpleSoundInstance.forUI(startSound.get(), 1));
		if (timer % 10 == 1 && durationSound.isPresent())
			mc.getSoundManager().play(SimpleSoundInstance.forUI(durationSound.get(), 1));
		if (timer == duration && impactSound.isPresent())
			mc.getSoundManager().play(SimpleSoundInstance.forUI(impactSound.get(), 1));

		if (entity instanceof LivingEntity living && specialAnimation != SpecialAnimation.WARDEN_SPAWN) {
			living.yBodyRotO = 0;
			living.yBodyRot = 0;
			living.yHeadRotO = 0;
			living.yHeadRot = 0;
			living.walkAnimation.update(0.3f, 1f);
		}
		if (entity instanceof EnderDragon dragon) {
			dragon.oFlapTime = dragon.flapTime;
			dragon.flapTime += 0.1;
		}
	}

}
