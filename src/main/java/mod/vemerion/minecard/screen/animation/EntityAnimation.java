package mod.vemerion.minecard.screen.animation;

import java.util.Optional;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.EvokerFangs;
import net.minecraft.world.phys.Vec2;

public class EntityAnimation extends Animation {

	private final Vec2 start;
	private ClientCard card;
	private int timer;
	private Entity entity;
	private final int duration;
	private final int size;
	private final int soundDelay;
	private Optional<SoundEvent> startSound;
	private Optional<SoundEvent> durationSound;
	private Optional<SoundEvent> impactSound;

	public EntityAnimation(Minecraft mc, Vec2 start, ClientCard card, EntityType<?> type, int duration, int size,
			int soundDelay, Optional<SoundEvent> startSound, Optional<SoundEvent> durationSound,
			Optional<SoundEvent> impactSound, Runnable onDone) {
		super(mc, onDone);
		this.start = start;
		this.card = card;
		this.duration = duration;
		this.size = size;
		this.soundDelay = soundDelay;
		this.startSound = startSound;
		this.durationSound = durationSound;
		this.impactSound = impactSound;
		this.entity = create(type);
	}

	private Entity create(EntityType<?> type) {
		if (type == EntityType.EVOKER_FANGS) {
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
		var target = new Vec2(card.getPosition().x + ClientCard.CARD_WIDTH / 2,
				card.getPosition().y + ClientCard.CARD_HEIGHT / 2);
		var pos = start == null ? target
				: new Vec2(Mth.lerp(progress, start.x, target.x), Mth.lerp(progress, start.y, target.y));
		poseStack.translate(pos.x, pos.y, 0);
		poseStack.scale(size, -size, size);
		poseStack.mulPose(new Quaternion(Mth.HALF_PI,
				(float) -Mth.atan2(target.y - pos.y, target.x - pos.x) + Mth.HALF_PI, -0.6f, false));

		mc.getEntityRenderDispatcher().getRenderer(entity).render(entity, 0, partialTick, poseStack, source,
				LightTexture.FULL_BRIGHT);
	}

	@Override
	public void tick() {
		timer++;
		if (timer == soundDelay && startSound.isPresent())
			mc.getSoundManager().play(SimpleSoundInstance.forUI(startSound.get(), 1));
		if (timer % 10 == 1 && durationSound.isPresent())
			mc.getSoundManager().play(SimpleSoundInstance.forUI(durationSound.get(), 1));
		if (timer == duration && impactSound.isPresent())
			mc.getSoundManager().play(SimpleSoundInstance.forUI(impactSound.get(), 1));

		if (entity instanceof LivingEntity living) {
			living.yBodyRotO = 0;
			living.yBodyRot = 0;
			living.yHeadRotO = 0;
			living.yHeadRot = 0;
			living.animationSpeedOld = 0.2f;
			living.animationSpeed = 0.2f;
			living.animationPosition = timer * 1.5f;
		}
	}

}
