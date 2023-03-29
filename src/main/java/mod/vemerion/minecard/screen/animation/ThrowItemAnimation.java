package mod.vemerion.minecard.screen.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class ThrowItemAnimation extends Animation {

	private static final int DURATION = 40;

	private ItemStack stack;
	private Vec2 start;
	private ClientCard target;
	private int timer = 0;

	public ThrowItemAnimation(Minecraft mc, ItemStack stack, Vec2 start, ClientCard target, Runnable onDone) {
		super(mc, onDone);
		this.stack = stack;
		this.start = start;
		this.target = target;
	}

	@Override
	public void tick() {
		timer++;
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var poseStack = new PoseStack();
		poseStack.pushPose();
		var progress = (timer + partialTick) / DURATION;
		poseStack.translate(Mth.lerp(progress, start.x, target.getPosition().x + ClientCard.CARD_WIDTH / 2),
				Mth.lerp(progress, start.y, target.getPosition().y + ClientCard.CARD_HEIGHT * 0.33), 50);
		float height = (-Mth.square(progress * 2 - 1) + 1) * 20;
		poseStack.scale(15 + height, -(15 + height), 15 + height);
		poseStack.mulPose(new Quaternion(start.y < target.getPosition().y ? (progress * 270) : (-progress * 270 + 180),
				0, 45, true));

		mc.getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.GUI, LightTexture.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY, poseStack, source, 0);
		poseStack.popPose();
	}

	@Override
	public boolean isDone() {
		return timer >= DURATION;
	}
}
