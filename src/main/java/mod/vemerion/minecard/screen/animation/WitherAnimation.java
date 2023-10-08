package mod.vemerion.minecard.screen.animation;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.common.util.TransformationHelper;

public class WitherAnimation extends Animation {

	private static record Part(Vec2 offset, Block block) {

	}

	private static final int DELAY = 7;
	private static final float SIZE = 70;
	private static final Part[] PARTS = { new Part(new Vec2(0, SIZE), Blocks.SOUL_SAND),
			new Part(new Vec2(-SIZE, 0), Blocks.SOUL_SAND), new Part(new Vec2(0, 0), Blocks.SOUL_SAND),
			new Part(new Vec2(SIZE, 0), Blocks.SOUL_SAND),
			new Part(new Vec2(-SIZE, -SIZE), Blocks.WITHER_SKELETON_SKULL),
			new Part(new Vec2(0, -SIZE), Blocks.WITHER_SKELETON_SKULL),
			new Part(new Vec2(SIZE, -SIZE), Blocks.WITHER_SKELETON_SKULL) };
	private static final int DURATION = DELAY * PARTS.length;

	private int timer;

	public WitherAnimation(Minecraft mc) {
		super(mc, () -> {
		});
	}

	@Override
	public boolean isDone() {
		return timer > DURATION;
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var poseStack = new PoseStack();
		poseStack.translate(mc.screen.width / 2, mc.screen.height / 2, 100);
		for (int i = 0; i < Math.min(PARTS.length, timer / DELAY + 1); i++) {
			var part = PARTS[i];
			poseStack.pushPose();
			poseStack.translate(part.offset.x, part.offset.y, i * -10);
			poseStack.mulPose(TransformationHelper.quatFromXYZ(10, 10, 0, true));
			poseStack.scale(SIZE, -SIZE, SIZE);
			mc.getItemRenderer().renderStatic(new ItemStack(part.block.asItem()), ItemDisplayContext.NONE,
					LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, source, null, 0);
			poseStack.popPose();
		}
	}

	@Override
	public void tick() {
		if (timer < DELAY * PARTS.length && timer % DELAY == 0) {
			var block = PARTS[timer / DELAY].block;
			mc.getSoundManager()
					.play(SimpleSoundInstance.forUI(block
							.getSoundType(block.defaultBlockState(), mc.level, BlockPos.ZERO, null).getPlaceSound(), 1,
							1));
		}
		if (timer == DURATION) {
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.WITHER_SPAWN, 1, 1));
		}
		timer++;
	}

}
