package mod.vemerion.minecard.screen.animation;

import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class BurnAnimation extends Animation {

	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/block/fire_1.png");
	private static final int WIDTH = ClientCard.CARD_WIDTH;
	private static final int HEIGHT = ClientCard.CARD_HEIGHT;
	private static final float Z = 0.1f;
	private static final int DISCARD_DURATION = 10;
	private static final int TEX_SIZE = 16;
	private static final int FRAMES = 512 / TEX_SIZE;

	private ClientCard card;
	private int timer;
	private int discardTimer;

	public BurnAnimation(Minecraft mc, ClientCard card, Runnable onDone) {
		super(mc, onDone);
		this.card = card;
	}

	@Override
	public boolean isDone() {
		return discardTimer > DISCARD_DURATION || card.isRemoved();
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var bufferbuilder = source.getBuffer(RenderType.text(TEXTURE));

		var pos = new Vec2(card.getPosition().x, card.getPosition().y);
		float discardProgress = discardTimer == 0 ? 0
				: Mth.clampedLerp(0, 1, (discardTimer + partialTick) / DISCARD_DURATION);
		float alpha = 0.5f;

		float index = timer % FRAMES;

		bufferbuilder.vertex(pos.x, pos.y + HEIGHT * (1 - discardProgress), Z).color(1, 1, 1, alpha)
				.uv(0, (index + (1 - discardProgress)) / FRAMES).uv2(LightTexture.FULL_BRIGHT).endVertex();
		bufferbuilder.vertex(pos.x + WIDTH, pos.y + HEIGHT * (1 - discardProgress), Z).color(1, 1, 1, alpha)
				.uv(1, (index + (1 - discardProgress)) / FRAMES).uv2(LightTexture.FULL_BRIGHT).endVertex();
		bufferbuilder.vertex(pos.x + WIDTH, pos.y, Z).color(1, 1, 1, alpha).uv(1, index / FRAMES)
				.uv2(LightTexture.FULL_BRIGHT).endVertex();
		bufferbuilder.vertex(pos.x, pos.y, Z).color(1, 1, 1, alpha).uv(0, index / FRAMES).uv2(LightTexture.FULL_BRIGHT)
				.endVertex();
	}

	@Override
	public void tick() {
		timer++;
		if (!card.hasProperty(CardProperty.BURN))
			discardTimer++;
	}

}
