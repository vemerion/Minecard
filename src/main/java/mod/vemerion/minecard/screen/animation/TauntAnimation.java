package mod.vemerion.minecard.screen.animation;

import java.util.List;

import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class TauntAnimation extends Animation {

	private static record Rectangle(float x, float y, float width, float height) {
	}

	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/mob_effect/resistance.png");
	private static final int SIZE = 45;
	private static final List<Rectangle> RECTANGLES = List.of(new Rectangle(0, 0, 0.5f, 0.27f),
			new Rectangle(0.28f, 0.27f, 0.22f, 0.46f), new Rectangle(0, 0.73f, 0.5f, 0.27f));
	private static final int MAX_OFFSET = 20;
	private static final int ANIMATION_DURATION = 10;

	private ClientCard card;
	private int startTimer;
	private int deathTimer;

	public TauntAnimation(Minecraft mc, ClientCard card, Runnable onDone) {
		super(mc, onDone);
		this.card = card;
	}

	@Override
	public boolean isDone() {
		return !card.hasProperty(CardProperty.TAUNT) || card.isRemoved();
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var bufferbuilder = source.getBuffer(RenderType.text(TEXTURE));

		float offset = Mth.clampedLerp(MAX_OFFSET, 0, (startTimer + partialTick) / ANIMATION_DURATION);
		var pos = new Vec2(card.getPosition().x + ClientCard.CARD_WIDTH / 2,
				card.getPosition().y + ClientCard.CARD_HEIGHT * -0.095f);
		float alpha = deathTimer == 0 ? 1 : Mth.clampedLerp(1, 0, (deathTimer + partialTick) / ANIMATION_DURATION);

		float z = 0;
		for (var rect : RECTANGLES) {
			// Right side
			bufferbuilder.vertex(pos.x + offset + SIZE * rect.x, pos.y + SIZE * (rect.y + rect.height), z)
					.color(1f, 1f, 1f, alpha).uv(0.5f + rect.x, rect.y + rect.height).uv2(LightTexture.FULL_BRIGHT)
					.endVertex();
			bufferbuilder
					.vertex(pos.x + offset + SIZE * (rect.x + rect.width), pos.y + SIZE * (rect.y + rect.height), z)
					.color(1f, 1f, 1f, alpha).uv(0.5f + rect.x + rect.width, rect.y + rect.height)
					.uv2(LightTexture.FULL_BRIGHT).endVertex();
			bufferbuilder.vertex(pos.x + offset + SIZE * (rect.x + rect.width), pos.y + SIZE * (rect.y), z)
					.color(1f, 1f, 1f, alpha).uv(0.5f + rect.x + rect.width, rect.y).uv2(LightTexture.FULL_BRIGHT)
					.endVertex();
			bufferbuilder.vertex(pos.x + offset + SIZE * (rect.x), pos.y + SIZE * (rect.y), z).color(1f, 1f, 1f, alpha)
					.uv(0.5f + rect.x, rect.y).uv2(LightTexture.FULL_BRIGHT).endVertex();

			// Left size
			bufferbuilder
					.vertex(pos.x - offset - SIZE * (rect.x + rect.width), pos.y + SIZE * (rect.y + rect.height), z)
					.color(1f, 1f, 1f, alpha).uv(0.5f - (rect.x + rect.width), rect.y + rect.height)
					.uv2(LightTexture.FULL_BRIGHT).endVertex();
			bufferbuilder.vertex(pos.x - offset - SIZE * (rect.x), pos.y + SIZE * (rect.y + rect.height), z)
					.color(1f, 1f, 1f, alpha).uv(0.5f - rect.x, rect.y + rect.height).uv2(LightTexture.FULL_BRIGHT)
					.endVertex();
			bufferbuilder.vertex(pos.x - offset - SIZE * (rect.x), pos.y + SIZE * (rect.y), z).color(1f, 1f, 1f, alpha)
					.uv(0.5f - rect.x, rect.y).uv2(LightTexture.FULL_BRIGHT).endVertex();
			bufferbuilder.vertex(pos.x - offset - SIZE * (rect.x + rect.width), pos.y + SIZE * (rect.y), z)
					.color(1f, 1f, 1f, alpha).uv(0.5f - (rect.x + rect.width), rect.y).uv2(LightTexture.FULL_BRIGHT)
					.endVertex();
		}
	}

	@Override
	public void tick() {
		startTimer++;
		if (startTimer == ANIMATION_DURATION)
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_LAND, 1f));
		if (card.isDead())
			deathTimer++;
	}

}
