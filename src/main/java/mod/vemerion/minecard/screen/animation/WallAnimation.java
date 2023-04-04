package mod.vemerion.minecard.screen.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.screen.ClientCard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource.BufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class WallAnimation extends Animation {

	private ClientCard card;
	private int timer;
	private ItemStack stack;

	public WallAnimation(Minecraft mc, ClientCard card, Runnable onDone) {
		super(mc, onDone);
		this.card = card;
		this.stack = new ItemStack(Items.COBBLESTONE);
	}

	@Override
	public boolean isDone() {
		return !card.hasProperty(CardProperty.SHIELD) || card.isRemoved();
	}

	@Override
	public void render(int mouseX, int mouseY, BufferSource source, float partialTick) {
		var poseStack = new PoseStack();
		poseStack.translate(card.getPosition().x + ClientCard.CARD_WIDTH * 0.4,
				card.getPosition().y + ClientCard.CARD_HEIGHT * 0.6, 0);
		for (int i = 0; i < Math.min(9, timer / 3); i++) {
			poseStack.pushPose();
			poseStack.translate(i % 3 * 6.3, -i / 3 * 3.3, 10 + i / 3 * 3);
			poseStack.mulPose(new Quaternion(60, 10, 0, true));
			poseStack.scale(6, 6, 6);
			mc.getItemRenderer().renderStatic(stack, ItemTransforms.TransformType.NONE, LightTexture.FULL_BRIGHT,
					OverlayTexture.NO_OVERLAY, poseStack, source, 0);
			poseStack.popPose();
		}
	}

	@Override
	public void tick() {
		timer++;
		if (timer <= 3 * 9 && timer % 3 == 0) {
			mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.STONE_PLACE, 1));
		}
	}

}
