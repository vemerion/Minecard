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

public class PoisonAnimation extends Animation {

	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/mob_effect/poison.png");
	private static final int WIDTH = ClientCard.CARD_WIDTH / 2;
	private static final int HEIGHT = ClientCard.CARD_HEIGHT / 2;
	private static final int Z = 10;
	private static final float SIZE = 15;

	private ClientCard card;
	private int timer;

	public PoisonAnimation(Minecraft mc, ClientCard card, Runnable onDone) {
		super(mc, onDone);
		this.card = card;
	}

	@Override
	public boolean isDone() {
		return !card.hasProperty(CardProperty.POISON) || card.isRemoved();
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var bufferbuilder = source.getBuffer(RenderType.text(TEXTURE));

		float progress = (timer + partialTick) / 50;
		float cos = Mth.cos(progress);
		float sin = Mth.sin(progress);
		float x = WIDTH * (Mth.abs(cos) * cos + Mth.abs(sin) * sin);
		float y = HEIGHT * (Mth.abs(cos) * cos - Mth.abs(sin) * sin);
		var pos = new Vec2(card.getPosition().x + x - SIZE / 2 + WIDTH, card.getPosition().y + y - SIZE / 2 + HEIGHT);

		float alpha = Mth.cos((timer + partialTick) / 3) * 0.25f + 0.75f;

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
