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

	private static final RenderType CARD_FRONT = RenderType
			.text(new ResourceLocation(Main.MODID, "textures/item/card_front.png"));
	private static final RenderType CARD_BACK = RenderType
			.text(new ResourceLocation(Main.MODID, "textures/item/card_back.png"));

	private BlockEntityRenderDispatcher dispatcher;

	public CardItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
		this.dispatcher = dispatcher;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void renderByItem(ItemStack stack, TransformType transform, PoseStack pose, MultiBufferSource buffer,
			int light, int overlay) {

		if (!(stack.getItem() instanceof CardItem card))
			return;

		Minecraft mc = Minecraft.getInstance();
		float partialTicks = mc.getFrameTime();

		// Render card
		if (transform == TransformType.GUI)
			pose.translate(0.01, -0.1, 0);
		pose.translate(0.1, 1, 0.45);
		pose.pushPose();
		pose.scale(CARD_SIZE, -CARD_SIZE, CARD_SIZE);
		renderCard(pose, CARD_FRONT, buffer, light);
		pose.scale(-1, 1, 1);
		pose.translate(-32, 0, 0);
		renderCard(pose, CARD_BACK, buffer, light);
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
		float maxWidth = 2;
		float maxHeight = 2;

		var type = card.getType(stack);
		var entity = type.create(mc.level);
		var dimensions = type.getDimensions();
		var widthScale = Math.min(1, maxWidth / dimensions.width);
		var heightScale = Math.min(1, maxHeight / dimensions.height);
		var scale = Math.min(widthScale, heightScale);

		pose.pushPose();
		pose.translate(0.4, -0.43, 0);
		pose.scale(0.15f * scale, 0.15f * scale, 0.15f * scale);
		pose.mulPose(new Quaternion(0, dispatcher.level.getGameTime() + partialTicks, 0, true));
		((EntityRenderer) mc.getEntityRenderDispatcher().renderers.get(type)).render(entity, 0, 0, pose, buffer, light);

		pose.popPose();
	}

	private void renderCard(PoseStack pose, RenderType card, MultiBufferSource buffer, int light) {
		Matrix4f matrix = pose.last().pose();
		VertexConsumer consumer = buffer.getBuffer(card);
		consumer.vertex(matrix, 0, 32, 0).color(255, 255, 255, 255).uv(0, 1).uv2(light).endVertex();
		consumer.vertex(matrix, 32, 32, 0).color(255, 255, 255, 255).uv(1, 1).uv2(light).endVertex();
		consumer.vertex(matrix, 32, 0, 0).color(255, 255, 255, 255).uv(1, 0).uv2(light).endVertex();
		consumer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255).uv(0, 0).uv2(light).endVertex();
	}
}
