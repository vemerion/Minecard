package mod.vemerion.minecard.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.item.CardItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public class CardItemRenderer extends BlockEntityWithoutLevelRenderer {

	private static final float TEXT_SIZE = 0.01f;
	private static final float CARD_SIZE = 0.025f;

	private static final RenderType CARD = RenderType.text(new ResourceLocation(Main.MODID, "textures/item/card.png"));

	private BlockEntityRenderDispatcher dispatcher;

	public CardItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		this.dispatcher = dispatcher;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void renderByItem(ItemStack stack, TransformType transform, PoseStack pose, MultiBufferSource buffer,
			int light, int overlay) {

		if (!(stack.getItem()instanceof CardItem card))
			return;

		Minecraft mc = Minecraft.getInstance();

		// Render card
		if (transform == TransformType.GUI)
			pose.translate(0.01, -0.1, 0);
		pose.translate(0.1, 1, 0.45);
		pose.pushPose();
		pose.scale(CARD_SIZE, -CARD_SIZE, CARD_SIZE);
		renderCard(pose, buffer, light);
		pose.scale(-1, 1, 1);
		pose.translate(-32, 0, 0);
		renderCard(pose, buffer, light);
		pose.popPose();

		// Render text
		pose.pushPose();
		pose.translate(0.1, -0.55, 0.01);
		pose.scale(TEXT_SIZE, -TEXT_SIZE, TEXT_SIZE);
		mc.font.drawWordWrap(card.getCardText(), 0, 0, 500, 0x000000);
		int y = 0;
		for (FormattedCharSequence line : mc.font.split(card.getCardText(), 50)) {
			mc.font.draw(pose, line, 0, y, 0x000000);
			y += 10;
		}
		pose.popPose();

		// Render entity
		pose.pushPose();
		pose.translate(0.4, -0.5, 0);
		pose.scale(0.15f, 0.15f, 0.15f);
		pose.mulPose(new Quaternion(0, dispatcher.level.getGameTime() + mc.getFrameTime(), 0, true));
		((EntityRenderer) mc.getEntityRenderDispatcher().renderers.get(card.getType())).render(card.getEntity(mc.level),
				0, 0, pose, buffer, light);

		pose.popPose();
	}

	private void renderCard(PoseStack pose, MultiBufferSource buffer, int light) {
		Matrix4f matrix = pose.last().pose();
		VertexConsumer consumer = buffer.getBuffer(CARD);
		consumer.vertex(matrix, 0, 32, 0).color(255, 255, 255, 255).uv(0, 1).uv2(light).endVertex();
		consumer.vertex(matrix, 32, 32, 0).color(255, 255, 255, 255).uv(1, 1).uv2(light).endVertex();
		consumer.vertex(matrix, 32, 0, 0).color(255, 255, 255, 255).uv(1, 0).uv2(light).endVertex();
		consumer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255).uv(0, 0).uv2(light).endVertex();
	}
}
