package mod.vemerion.minecard.renderer;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.AdditionalCardData;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.item.CardItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CardItemRenderer extends BlockEntityWithoutLevelRenderer {

	private static final Map<EntityType<?>, Entity> CACHE = new HashMap<>();

	private static final float TEXT_SIZE = 0.01f;
	private static final float TITLE_SIZE = 0.005f;
	private static final float CARD_SIZE = 0.025f;

	private static final RenderType CARD_FRONT = RenderType
			.text(new ResourceLocation(Main.MODID, "textures/item/card_front.png"));
	private static final RenderType CARD_BACK = RenderType
			.text(new ResourceLocation(Main.MODID, "textures/item/card_back.png"));

	public CardItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void renderCard(Card card, TransformType transform, PoseStack pose, MultiBufferSource buffer,
			int light, int overlay) {
		Minecraft mc = Minecraft.getInstance();

		// Render card
		if (transform == TransformType.GUI)
			pose.translate(0.01, -0.1, 0);
		if (transform != TransformType.NONE)
			pose.translate(0.1, 1, 0.45);
		pose.pushPose();
		pose.scale(CARD_SIZE, -CARD_SIZE, CARD_SIZE);
		renderCard(pose, CARD_FRONT, buffer, light);
		pose.scale(-1, 1, 1);
		pose.translate(-32, 0, 0);
		renderCard(pose, CARD_BACK, buffer, light);
		pose.popPose();

		var type = card.getType();
		if (type == null)
			return;
		var entity = getEntity(card, mc.level);

		// Render title
		pose.pushPose();
		pose.translate(0.2, -0.065, 0.01);
		pose.scale(TITLE_SIZE, -TITLE_SIZE, TITLE_SIZE);
		mc.font.draw(pose, entity.getName(), 0, 0, 0x000000);
		pose.popPose();

		// Render text
//		pose.pushPose();
//		pose.translate(0.1, -0.55, 0.01);
//		pose.scale(TEXT_SIZE, -TEXT_SIZE, TEXT_SIZE);
//		int y = 0;
//		for (FormattedCharSequence line : mc.font.split(type.getDescription(), 50)) {
//			mc.font.draw(pose, line, 0, y, 0);
//			y += 10;
//		}
//		pose.popPose();

		// Render values
		var itemRenderer = mc.getItemRenderer();
		renderValue(itemRenderer, mc.font, Items.EMERALD, card.getCost(), 0.62f, -0.14f, light, overlay, pose, buffer);
		renderValue(itemRenderer, mc.font, Items.STONE_SWORD, card.getDamage(), 0.21f, -0.42f, light, overlay, pose,
				buffer);
		renderValue(itemRenderer, mc.font, Items.GLISTERING_MELON_SLICE, card.getHealth(), 0.6f, -0.42f, light, overlay,
				pose, buffer);

		// Ready
		if (card.isReady()) {
			pose.pushPose();
			pose.translate(0.43, 0.07, 0);
			pose.scale(0.3f, 0.3f, 0.3f);
			itemRenderer.renderStatic(new ItemStack(Items.CARROT_ON_A_STICK), TransformType.NONE, light, overlay, pose,
					buffer, 0);
			pose.popPose();
		}

		// Render entity
		float maxWidth = 2;
		float maxHeight = 2;

		var dimensions = type.getDimensions();
		var widthScale = Math.min(1, maxWidth / dimensions.width);
		var heightScale = Math.min(1, maxHeight / dimensions.height);
		var scale = Math.min(widthScale, heightScale);

		pose.pushPose();
		pose.translate(0.4, -0.43, 0);
		pose.scale(0.15f * scale, 0.15f * scale, 0.15f * scale);
		pose.mulPose(new Quaternion(0, 20, 0, true));
		((EntityRenderer) mc.getEntityRenderDispatcher().getRenderer(entity)).render(entity, 0, 0, pose, buffer, light);
		pose.popPose();
	}

	private static void renderValue(ItemRenderer itemRenderer, Font font, Item item, int value, float x, float y,
			int light, int overlay, PoseStack poseStack, MultiBufferSource buffer) {
		poseStack.pushPose();
		poseStack.translate(x, y, 0);

		// Item
		poseStack.pushPose();
		poseStack.scale(0.2f, 0.2f, 0.2f);
		itemRenderer.renderStatic(new ItemStack(item), TransformType.NONE, light, overlay, poseStack, buffer, 0);
		poseStack.popPose();

		// Text
		var text = String.valueOf(value);
		var offset = -font.width(text) * TEXT_SIZE / 2;
		poseStack.translate(offset, 0.03, 0.01);
		poseStack.scale(TEXT_SIZE, -TEXT_SIZE, TEXT_SIZE);

		// Shadow
		poseStack.pushPose();
		poseStack.translate(0.7, 0.7, 0);
		font.draw(poseStack, String.valueOf(value), 0, 0, 0);
		poseStack.popPose();

		// Actual text
		poseStack.translate(0, 0, 0.01);
		font.draw(poseStack, String.valueOf(value), 0, 0, 0xFFFFFF);
		poseStack.popPose();
	}

	public static Entity getEntity(Card card, ClientLevel level) {
		var type = card.getType();
		if (type == EntityType.PLAYER) {
			if (card.getAdditionalData() instanceof AdditionalCardData.IdData idData) {
				for (var player : level.players())
					if (player.getUUID().equals(idData.getId()))
						return player;
			}
			return level.players().get(0);
		}

		return CACHE.computeIfAbsent(type, t -> t.create(level));
	}

	@Override
	public void renderByItem(ItemStack stack, TransformType transform, PoseStack pose, MultiBufferSource buffer,
			int light, int overlay) {

		if (!(stack.getItem() instanceof CardItem card))
			return;

		renderCard(Cards.getInstance().get(card.getType(stack)).getCardForRendering(), transform, pose, buffer, light,
				overlay);
	}

	private static void renderCard(PoseStack pose, RenderType card, MultiBufferSource buffer, int light) {
		Matrix4f matrix = pose.last().pose();
		VertexConsumer consumer = buffer.getBuffer(card);
		consumer.vertex(matrix, 0, 32, 0).color(255, 255, 255, 255).uv(0, 1).uv2(light).endVertex();
		consumer.vertex(matrix, 32, 32, 0).color(255, 255, 255, 255).uv(1, 1).uv2(light).endVertex();
		consumer.vertex(matrix, 32, 0, 0).color(255, 255, 255, 255).uv(1, 0).uv2(light).endVertex();
		consumer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255).uv(0, 0).uv2(light).endVertex();
	}
}
