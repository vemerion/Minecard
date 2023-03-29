package mod.vemerion.minecard.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.renderer.CardItemRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec2;

public class ClientCard extends Card {

	public static final int CARD_SCALE = GameScreen.CARD_SCALE;
	public static final int CARD_LIGHT = GameScreen.CARD_LIGHT;
	public static final int CARD_LIGHT_HOVER = GameScreen.CARD_LIGHT_HOVER;
	public static final int CARD_WIDTH = GameScreen.CARD_WIDTH;
	public static final int CARD_HEIGHT = GameScreen.CARD_HEIGHT;

	private Vec2 position;
	private Vec2 position0;
	private Vec2 targetPosition;
	private GameScreen screen;

	public ClientCard(Card card, Vec2 position, GameScreen screen) {
		super(card.getType(), card.getCost(), card.getHealth(), card.getDamage(), card.isReady(),
				card.getAdditionalData());
		this.position = position;
		this.position0 = position;
		this.targetPosition = position;
		this.screen = screen;
	}

	public void tick() {
		position0 = position;
		position = new Vec2((float) Mth.lerp(0.9, position.x, targetPosition.x),
				(float) Mth.lerp(0.9, position.y, targetPosition.y));
	}

	private Vec2 getPosition(float partialTick) {
		return new Vec2((float) Mth.lerp(partialTick, position0.x, position.x),
				(float) Mth.lerp(partialTick, position0.y, position.y));
	}

	public Vec2 getPosition() {
		return position;
	}

	public void setPosition(Vec2 position) {
		this.targetPosition = position;
	}

	public void render(PoseStack ps, int mouseX, int mouseY, BufferSource source, float partialTick) {
		ps.pushPose();

		var pos = this == screen.getSelectedCard() ? new Vec2(mouseX - CARD_WIDTH / 2, mouseY - CARD_HEIGHT / 2)
				: getPosition(partialTick);

		// Rotate to show back
		if (getType() == null) {
			ps.translate(pos.x + 24, 0, 0);
			ps.mulPose(new Quaternion(0, 180, 0, true));
			ps.translate(-pos.x - 24, 0, 0);
		}

		ps.translate(pos.x, pos.y, 0);
		ps.scale(CARD_SCALE, -CARD_SCALE, CARD_SCALE);
		int light = contains(mouseX, mouseY) ? CARD_LIGHT : CARD_LIGHT_HOVER;
		CardItemRenderer.renderCard(this, TransformType.NONE, ps, source, light, OverlayTexture.NO_OVERLAY);
		ps.popPose();

		// Attacking card
		if (this == screen.getAttackingCard()) {
			ps.pushPose();
			ps.translate(pos.x + CARD_WIDTH / 2 + 1, pos.y + CARD_WIDTH / 2 - 2, 50);
			ps.scale(30, -30, 30);
			screen.getMinecraft().getItemRenderer().renderStatic(new ItemStack(Items.NETHERITE_SWORD),
					TransformType.NONE, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ps, source, 0);
			ps.scale(-1, 1, 1);
			screen.getMinecraft().getItemRenderer().renderStatic(new ItemStack(Items.NETHERITE_SWORD),
					TransformType.NONE, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ps, source, 0);
			ps.popPose();
		}

	}

	public boolean contains(double pMouseX, double pMouseY) {
		return pMouseX > position.x && pMouseX < position.x + CARD_WIDTH && pMouseY > position.y
				&& pMouseY < position.y + CARD_HEIGHT;
	}

}