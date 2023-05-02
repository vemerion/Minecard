package mod.vemerion.minecard.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;

public class CardGameRobot extends Mob {

	private static final int HEAD_ROT_DURATION = 60;
	private static final int LEG_STRETCH_DURATION = 100;

	private int headRotTimer = HEAD_ROT_DURATION;
	private int legStretchTimer = LEG_STRETCH_DURATION;

	public CardGameRobot(EntityType<? extends CardGameRobot> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder attributes() {
		return Mob.createMobAttributes();
	}

	public void guiTick() {
		if (headRotTimer == HEAD_ROT_DURATION) {
			if (random.nextFloat() < 0.005f) {
				headRotTimer = 0;
			}
		} else {
			headRotTimer++;
		}

		if (legStretchTimer == LEG_STRETCH_DURATION) {
			if (random.nextFloat() < 0.005f) {
				legStretchTimer = 0;
			}
		} else {
			legStretchTimer++;
		}
	}

	public float getHeadRot(float partialTick) {
		return headRotTimer == HEAD_ROT_DURATION ? 0 : (headRotTimer + partialTick) / HEAD_ROT_DURATION * Mth.TWO_PI;
	}

	public float getLegOffset(float partialTick) {
		return legStretchTimer == LEG_STRETCH_DURATION ? 0
				: Mth.abs(Mth.sin((legStretchTimer + partialTick) / (LEG_STRETCH_DURATION / 2) * Mth.PI) * 5);
	}

	public int getLegTimer() {
		return legStretchTimer;
	}

}
