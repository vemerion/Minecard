package mod.vemerion.minecard.screen.animation;

import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public class AttackAnimation extends Animation {

	private ClientCard attacker;
	private ClientCard target;
	private boolean done = false;

	public AttackAnimation(Minecraft mc, ClientCard attacker, ClientCard target) {
		super(mc, () -> {
		});
		this.attacker = attacker;
		this.target = target;
		this.attacker.setTarget(target);
	}

	@Override
	public boolean isDone() {
		return done;
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
	}

	@Override
	public void tick() {
		if (attacker.contains(target.getPosition().x + ClientCard.CARD_WIDTH / 2,
				target.getPosition().y + ClientCard.CARD_HEIGHT / 2)) {
			done = true;
			attacker.setTarget(null);
			float value = attacker.getDamage() / 10f;
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.GENERIC_HURT,
					Mth.clampedLerp(1.5f, 1f, value), Mth.clampedLerp(0.7f, 1f, value)));
		}
	}

}
