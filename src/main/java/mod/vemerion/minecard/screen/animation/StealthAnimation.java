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

public class StealthAnimation extends Animation {

	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/environment/clouds.png");
	private static final int WIDTH = (int) (ClientCard.CARD_WIDTH * 0.95);
	private static final int HEIGHT = ClientCard.CARD_HEIGHT;
	private static final int Z = 3;
	private static final int DISCARD_DURATION = 100;
	private static final int SPEED = 1000;
	private static final float UV = 0.2f;
	private static final float RED = 0.3f;
	private static final float GREEN = 0.3f;
	private static final float BLUE = 0.3f;

	private ClientCard card;
	private int timer;
	private int discardTimer;

	public StealthAnimation(Minecraft mc, ClientCard card, Runnable onDone) {
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

		var pos = new Vec2(card.getPosition().x + ClientCard.CARD_WIDTH * 0.05f, card.getPosition().y);
		float progress = -(timer + partialTick) / SPEED;
		float discardProgress = discardTimer == 0 ? 0
				: Mth.clampedLerp(0, 1, (discardTimer + partialTick) / DISCARD_DURATION);
		float alpha = Mth.lerp(discardProgress, 1, 0.2f);

		bufferbuilder.vertex(pos.x + discardProgress * WIDTH, pos.y + HEIGHT, Z).color(RED, GREEN, BLUE, alpha)
				.uv(progress, UV).uv2(LightTexture.FULL_BRIGHT).endVertex();
		bufferbuilder.vertex(pos.x + WIDTH, pos.y + HEIGHT, Z).color(RED, GREEN, BLUE, alpha)
				.uv(progress + UV - discardProgress * UV, UV).uv2(LightTexture.FULL_BRIGHT).endVertex();
		bufferbuilder.vertex(pos.x + WIDTH, pos.y, Z).color(RED, GREEN, BLUE, alpha)
				.uv(progress + UV - discardProgress * UV, 0).uv2(LightTexture.FULL_BRIGHT).endVertex();
		bufferbuilder.vertex(pos.x + discardProgress * WIDTH, pos.y, Z).color(RED, GREEN, BLUE, alpha).uv(progress, 0)
				.uv2(LightTexture.FULL_BRIGHT).endVertex();
	}

	@Override
	public void tick() {
		if (!card.hasProperty(CardProperty.STEALTH))
			discardTimer++;
		else
			timer++;
	}

}
