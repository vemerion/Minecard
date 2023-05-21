package mod.vemerion.minecard.screen.animation;

import com.mojang.blaze3d.vertex.PoseStack;

import mod.vemerion.minecard.renderer.CardItemRenderer;
import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class HealthAnimation extends Animation {

	private static final int DURATION = 20 * 3;

	private ClientCard card;
	private int value;
	private int start;
	private int timer;
	private TextComponent text;

	public HealthAnimation(Minecraft mc, ClientCard card, int value) {
		super(mc, () -> {
		});
		this.card = card;
		this.value = value;
		this.start = card.getHealth();
		this.text = new TextComponent((value > 0 ? "+" : "") + String.valueOf(value));
	}

	@Override
	public boolean isDone() {
		return timer > DURATION || card.isRemoved() || value == 0 || start != card.getHealth();
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		if (value == 0)
			return;
		
		float progress = (timer + partialTick) / DURATION;
		var poseStack = new PoseStack();
		var scale = Mth.lerp(progress, 2f, 0);
		var pos = new Vec2(card.getPosition().x + (ClientCard.CARD_WIDTH - mc.font.width(text) * scale) / 2,
				card.getPosition().y + (ClientCard.CARD_HEIGHT - mc.font.lineHeight * scale) / 2);
		poseStack.translate(pos.x, pos.y, 100);
		poseStack.scale(scale, scale, 1);
		mc.font.drawShadow(poseStack, text, 0, 0,
				value < 0 ? CardItemRenderer.BAD_VALUE_COLOR : CardItemRenderer.GOOD_VALUE_COLOR);
	}

	@Override
	public void tick() {
		timer++;
	}

}
