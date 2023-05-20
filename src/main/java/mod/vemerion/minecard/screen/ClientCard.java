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
	private static final int LEFT_OFFSET = 3; // To get rid of padding in texture

	private Vec2 position;
	private Vec2 position0;
	private Vec2 targetPosition;
	private ClientCard attackingTarget;
	private GameScreen screen;
	private boolean removed;
	private float size = 1;
	private float size0 = 1;
	private float maxSize = 1;

	public ClientCard(Card card, Vec2 position, GameScreen screen) {
		super(card.getType(), card.getCost(), card.getOriginalCost(), card.getHealth(), card.getMaxHealth(),
				card.getOriginalHealth(), card.getDamage(), card.getOriginalDamage(), card.isReady(),
				card.getProperties(), card.getAbility(), card.getEquipment(), card.getAdditionalData());
		this.setId(card.getId());
		this.position = position;
		this.position0 = position;
		this.targetPosition = position;
		this.screen = screen;
	}

	public void tick() {
		position0 = position;
		lerpPos(attackingTarget != null ? Math.max(0.25 - getDamage() / 100f, 0.15) : 0.9,
				attackingTarget != null ? attackingTarget.getPosition() : targetPosition);
		size0 = size;
		size = Mth.lerp(0.1f, size, maxSize);
	}

	private void lerpPos(double value, Vec2 target) {
		position = new Vec2((float) Mth.lerp(value, position.x, target.x),
				(float) Mth.lerp(value, position.y, target.y));
	}

	private Vec2 getPosition(float partialTick) {
		return new Vec2((float) Mth.lerp(partialTick, position0.x, position.x),
				(float) Mth.lerp(partialTick, position0.y, position.y));
	}

	private float getSize(float partialTick) {
		return Mth.lerp(partialTick, size0, size);
	}

	public Vec2 getPosition() {
		return position;
	}

	public void setPosition(Vec2 position) {
		this.targetPosition = position;
	}

	public void setTarget(ClientCard target) {
		this.attackingTarget = target;
	}

	public void resetPosition() {
		this.position = targetPosition;
	}

	public void remove() {
		this.removed = true;
	}

	public boolean isRemoved() {
		return removed;
	}

	public void appear(float maxSize) {
		size = 0;
		size0 = 0;
		this.maxSize = maxSize;
	}

	public void render(PoseStack ps, int mouseX, int mouseY, BufferSource source, float partialTick) {
		ps.pushPose();
		ps.translate(-LEFT_OFFSET, 0, 0);

		var pos = this == screen.getSelectedCard() ? new Vec2(mouseX - CARD_WIDTH / 2, mouseY - CARD_HEIGHT / 2)
				: getPosition(partialTick);

		// Rotate to show back
		if (getType() == null) {
			ps.translate(pos.x + 24, 0, 0);
			ps.mulPose(new Quaternion(0, 180, 0, true));
			ps.translate(-pos.x - 24, 0, 0);
		}

		ps.translate(pos.x, pos.y, 0);

		var currentSize = getSize(partialTick) * CARD_SCALE;
		ps.translate((1 - getSize(partialTick)) * CARD_WIDTH / 2, (1 - getSize(partialTick)) * CARD_HEIGHT / 2, 0);
		ps.scale(currentSize, -currentSize, currentSize);

		int light = (contains(mouseX, mouseY) ? CARD_LIGHT : CARD_LIGHT_HOVER);
		CardItemRenderer.renderCard(this, TransformType.NONE, ps, source, light, OverlayTexture.NO_OVERLAY);
		ps.popPose();

		// Attacking card
		if (this == screen.getAttackingCard()) {
			ps.pushPose();
			ps.translate(pos.x + CARD_WIDTH / 2, pos.y + CARD_HEIGHT / 2 - 2, 50);
			ps.scale(30, -30, 30);
			screen.getMinecraft().getItemRenderer().renderStatic(new ItemStack(Items.NETHERITE_SWORD),
					TransformType.NONE, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ps, source, 0);
			ps.scale(-1, 1, 1);
			screen.getMinecraft().getItemRenderer().renderStatic(new ItemStack(Items.NETHERITE_SWORD),
					TransformType.NONE, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ps, source, 0);
			ps.popPose();
		}
//		GuiComponent.fill(ps, (int) getPosition().x, (int) getPosition().y, (int) getPosition().x + CARD_WIDTH,
//				(int) getPosition().y + CARD_HEIGHT, FastColor.ARGB32.color(255, 255, 255, 255));
	}

	public boolean contains(double x, double y) {
		var size = getSize(0);
		var widthOffset = (size - 1) * CARD_WIDTH / 2;
		var heightOffset = (size - 1) * CARD_HEIGHT / 2;
		return x > position.x - widthOffset && x < position.x + CARD_WIDTH + widthOffset
				&& y > position.y - heightOffset && y < position.y + CARD_HEIGHT + heightOffset;
	}
}