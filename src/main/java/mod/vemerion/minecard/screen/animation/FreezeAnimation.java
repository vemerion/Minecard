package mod.vemerion.minecard.screen.animation;

import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;

public class FreezeAnimation extends Animation {

	private static final ResourceLocation TEXTURE = new ResourceLocation("textures/environment/snow.png");
	private static final int WIDTH = ClientCard.CARD_WIDTH;
	private static final int HEIGHT = ClientCard.CARD_HEIGHT;
	private static final float V = HEIGHT / (WIDTH * 4f);
	private static final int Z = 1;

	private ClientCard card;
	private int timer;

	public FreezeAnimation(Minecraft mc, ClientCard card, Runnable onDone) {
		super(mc, onDone);
		this.card = card;
	}

	@Override
	public boolean isDone() {
		return !card.hasProperty(CardProperty.FREEZE) || card.isRemoved();
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var bufferbuilder = source.getBuffer(RenderType.text(TEXTURE));

		var pos = new Vec2(card.getPosition().x + ClientCard.CARD_WIDTH * 0.05f, card.getPosition().y);

		float progress = -(timer + partialTick) / 200;

		bufferbuilder.vertex(pos.x, pos.y + HEIGHT, Z).color(1f, 1f, 1f, 1f).uv(0, progress + V)
				.uv2(LightTexture.FULL_BRIGHT).endVertex();
		bufferbuilder.vertex(pos.x + WIDTH, pos.y + HEIGHT, Z).color(1f, 1f, 1f, 1f).uv(1, progress + V)
				.uv2(LightTexture.FULL_BRIGHT).endVertex();
		bufferbuilder.vertex(pos.x + WIDTH, pos.y, Z).color(1f, 1f, 1f, 1f).uv(1, progress)
				.uv2(LightTexture.FULL_BRIGHT).endVertex();
		bufferbuilder.vertex(pos.x, pos.y, Z).color(1f, 1f, 1f, 1f).uv(0, progress).uv2(LightTexture.FULL_BRIGHT)
				.endVertex();
	}

	@Override
	public void tick() {
		timer++;
	}

}
