package mod.vemerion.minecard.screen.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;

public class ChargeAnimation extends Animation {

	private static final int DURATION = 25;

	private final Vec2 start;
	private ClientCard card;
	private int timer;
	private Entity entity;

	public ChargeAnimation(Minecraft mc, Vec2 start, ClientCard card, EntityType<?> type, Runnable onDone) {
		super(mc, onDone);
		this.start = start;
		this.card = card;
		this.entity = type.create(mc.level);
	}

	@Override
	public boolean isDone() {
		return timer >= DURATION;
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var poseStack = new PoseStack();
		float progress = (timer + partialTick) / DURATION;
		var target = new Vec2(card.getPosition().x + ClientCard.CARD_WIDTH / 2,
				card.getPosition().y + ClientCard.CARD_HEIGHT / 2);
		var pos = new Vec2(Mth.lerp(progress, start.x, target.x), Mth.lerp(progress, start.y, target.y));
		poseStack.translate(pos.x, pos.y, 0);
		poseStack.scale(25, -25, 25);
		poseStack.mulPose(new Quaternion(Mth.HALF_PI,
				(float) -Mth.atan2(target.y - pos.y, target.x - pos.x) + Mth.HALF_PI, -0.6f, false));

		mc.getEntityRenderDispatcher().getRenderer(entity).render(entity, 0, partialTick, poseStack, source,
				LightTexture.FULL_BRIGHT);
	}

	@Override
	public void tick() {
		timer++;
		if (timer % 10 == 1)
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.HORSE_GALLOP, 1));
		if (timer == DURATION)
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.HOSTILE_BIG_FALL, 1));

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
