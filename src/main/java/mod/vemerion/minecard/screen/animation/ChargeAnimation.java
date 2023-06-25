package mod.vemerion.minecard.screen.animation;

import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class ChargeAnimation extends Animation {

	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/mob_effect/speed.png");
	private static final int SIZE = 25;
	private static final int DURATION = 45;
	private static final int Z = 2;

	private ClientCard card;
	private int timer;

	public ChargeAnimation(Minecraft mc, ClientCard card, Runnable onDone) {
		super(mc, onDone);
		this.card = card;
	}

	@Override
	public boolean isDone() {
		return card.isRemoved() || timer > DURATION;
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var bufferbuilder = source.getBuffer(RenderType.text(TEXTURE));

		var pos = new Vec2(card.getPosition(partialTick).x + (ClientCard.CARD_WIDTH - SIZE) / 2,
				card.getPosition(partialTick).y + (ClientCard.CARD_HEIGHT - SIZE) / 2);
		float alpha = Mth.cos((timer + partialTick) / 10 * Mth.TWO_PI) / 3 + (1 - 2f / 3);

		bufferbuilder.vertex(pos.x, pos.y + SIZE, Z).color(1f, 1f, 1f, alpha).uv(0, 1).uv2(LightTexture.FULL_BRIGHT)
				.endVertex();
		bufferbuilder.vertex(pos.x + SIZE, pos.y + SIZE, Z).color(1f, 1f, 1f, alpha).uv(1, 1)
				.uv2(LightTexture.FULL_BRIGHT).endVertex();
		bufferbuilder.vertex(pos.x + SIZE, pos.y, Z).color(1f, 1f, 1f, alpha).uv(1, 0).uv2(LightTexture.FULL_BRIGHT)
				.endVertex();
		bufferbuilder.vertex(pos.x, pos.y, Z).color(1f, 1f, 1f, alpha).uv(0, 0).uv2(LightTexture.FULL_BRIGHT)
				.endVertex();
	}

	@Override
	public void tick() {
		timer++;
	}

}
