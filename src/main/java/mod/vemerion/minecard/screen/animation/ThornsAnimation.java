package mod.vemerion.minecard.screen.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.client.model.data.EmptyModelData;

public class ThornsAnimation extends Animation {

	private static final Vec2[] OFFSETS = { new Vec2(0, 20), new Vec2(30, 26), new Vec2(-2, 50), new Vec2(32, 45),
			new Vec2(10, 7), };
	private static final int SCALE = 12;
	private static final int ROTATION = 130;

	private ClientCard card;
	private BlockState state;

	public ThornsAnimation(Minecraft mc, ClientCard card, Runnable onDone) {
		super(mc, onDone);
		this.card = card;
		this.state = Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue(BlockStateProperties.DRIPSTONE_THICKNESS,
				DripstoneThickness.TIP);
	}

	@Override
	public boolean isDone() {
		return !card.hasProperty(CardProperty.THORNS) || card.isRemoved();
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var poseStack = new PoseStack();
		poseStack.translate(card.getPosition().x, card.getPosition().y, 20);
		for (var offset : OFFSETS) {
			poseStack.pushPose();
			poseStack.translate(offset.x, offset.y, 0);
			poseStack.scale(SCALE, SCALE, SCALE);
			poseStack.mulPose(new Quaternion(ROTATION, 0, 0, true));
			mc.getBlockRenderer().renderSingleBlock(state, poseStack, source, LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
			poseStack.popPose();
		}
	}

}
