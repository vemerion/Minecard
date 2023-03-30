package mod.vemerion.minecard.screen.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.EvokerFangs;

public class EvokerFangsAnimation extends Animation {

	private static final int DURATION = 30;

	private ClientCard card;
	private int timer;
	private EvokerFangs entity;

	public EvokerFangsAnimation(Minecraft mc, ClientCard card, Runnable onDone) {
		super(mc, onDone);
		this.card = card;
		this.entity = new EvokerFangs(EntityType.EVOKER_FANGS, mc.level) {
			@Override
			public float getAnimationProgress(float pPartialTicks) {
				return (timer + pPartialTicks) / DURATION;
			};
		};
	}

	@Override
	public boolean isDone() {
		return timer >= DURATION;
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var poseStack = new PoseStack();
		poseStack.translate(card.getPosition().x + ClientCard.CARD_WIDTH / 2,
				card.getPosition().y + ClientCard.CARD_HEIGHT / 2, 0);
		poseStack.scale(30, -30, 30);
		poseStack.mulPose(new Quaternion(90, 30, 10, true));

		mc.getEntityRenderDispatcher().getRenderer(entity).render(entity, 0, partialTick, poseStack, source,
				LightTexture.FULL_BRIGHT);
	}

	@Override
	public void tick() {
		timer++;
		if (timer == 10)
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EVOKER_FANGS_ATTACK, 1.5f));
	}

}
