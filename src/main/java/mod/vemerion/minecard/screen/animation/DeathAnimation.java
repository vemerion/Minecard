package mod.vemerion.minecard.screen.animation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.renderer.CardItemRenderer;
import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class DeathAnimation extends Animation {

	private static final Method GET_DEATH_SOUND = ObfuscationReflectionHelper.findMethod(LivingEntity.class, "m_5592_");

	private static final int DURATION = 30;

	private ClientCard card;
	private int timer;

	public DeathAnimation(Minecraft mc, ClientCard card, int delay, Runnable onDone) {
		super(mc, onDone);
		this.card = card;
		this.timer = -delay;
	}

	@Override
	public boolean isDone() {
		return timer >= DURATION;
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var poseStack = new PoseStack();
		var progress = timer <= 0 ? 0 : (timer + partialTick) / DURATION;
		poseStack.translate(card.getPosition().x + ClientCard.CARD_WIDTH / 2,
				card.getPosition().y + ClientCard.CARD_HEIGHT / 2, 0);
		poseStack.mulPose(new Quaternion(0, 0, Mth.sin(progress * 50) * 30, true));
		poseStack.translate(-(card.getPosition().x + ClientCard.CARD_WIDTH / 2),
				-(card.getPosition().y + ClientCard.CARD_HEIGHT / 2), 0);
		card.render(poseStack, mouseX, mouseY, source, partialTick);
	}

	@Override
	public void tick() {
		timer++;
		if (timer == DURATION) {
			playDeathSound();
		}
	}

	private void playDeathSound() {
		if (CardItemRenderer.getEntity(card, mc.level) instanceof LivingEntity entity) {
			try {
				var sound = GET_DEATH_SOUND.invoke(entity) instanceof SoundEvent deathSound ? deathSound
						: SoundEvents.GENERIC_DEATH;
				mc.getSoundManager().play(SimpleSoundInstance.forUI(sound, 1));
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				Main.LOGGER.error("Unable to play death sound for card " + card.getType().getRegistryName().toString()
						+ ": " + e);
			}
		}
	}

}
