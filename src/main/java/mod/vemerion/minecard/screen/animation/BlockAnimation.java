package mod.vemerion.minecard.screen.animation;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;

public class BlockAnimation extends Animation {

	private static final float SIZE = 20;

	private Block block;
	private List<Vec2> positions;
	private int timer;

	public BlockAnimation(Minecraft mc, Block block, AABB area) {
		super(mc, () -> {
		});
		this.block = block;
		init(area);
	}

	private void init(AABB area) {
		positions = new ArrayList<>();
		for (double i = area.minX; i < area.maxX; i += SIZE) {
			for (double j = area.minY; j < area.maxY; j += SIZE) {
				positions.add(new Vec2((float) i, (float) j));
			}
		}

	}

	@Override
	public boolean isDone() {
		return timer > positions.size() * 2;
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var poseStack = new PoseStack();
		poseStack.translate(0, 0, 0);
		for (int i = Math.max(0, timer - positions.size()); i < Math.min(timer, positions.size()); i++) {
			var pos = positions.get(i);
			poseStack.pushPose();
			poseStack.translate(pos.x, pos.y, 0);
			poseStack.mulPose(new Quaternion(10, 10, 0, true));
			poseStack.scale(SIZE, -SIZE, SIZE);
			mc.getItemRenderer().renderStatic(new ItemStack(block), ItemTransforms.TransformType.NONE,
					LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, source, 0);
			poseStack.popPose();
		}
	}

	@Override
	public void tick() {
		if (timer < positions.size())
			mc.getSoundManager()
					.play(SimpleSoundInstance.forUI(block
							.getSoundType(block.defaultBlockState(), mc.level, BlockPos.ZERO, null).getPlaceSound(), 1,
							1));
		else if (timer < positions.size() * 2)
			mc.getSoundManager()
					.play(SimpleSoundInstance.forUI(block
							.getSoundType(block.defaultBlockState(), mc.level, BlockPos.ZERO, null).getBreakSound(), 1,
							1));
		timer++;
	}

}
