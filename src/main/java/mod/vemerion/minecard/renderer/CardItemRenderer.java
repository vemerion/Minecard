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
import mod.vemerion.minecard.game.CardProperty;
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
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class CardItemRenderer extends BlockEntityWithoutLevelRenderer {

	private static record DescriptionSize(float scale, int maxWidth, int maxLines) {
	}

	private static final DescriptionSize[] DESCRIPTION_SIZES = { new DescriptionSize(0.007f, 85, 3),
			new DescriptionSize(0.006f, 98, 4), new DescriptionSize(0.004f, 148, 6) };

	private static final Map<EntityType<?>, Entity> CACHE = new HashMap<>();

	private static final float TEXT_SIZE = 0.01f;
	private static final float TITLE_SIZE = 0.005f;
	private static final float CARD_SIZE = 0.025f;

	private static final RenderType CARD_FRONT = RenderType
			.text(new ResourceLocation(Main.MODID, "textures/item/card_front.png"));
	private static final RenderType CARD_BACK = RenderType
			.text(new ResourceLocation(Main.MODID, "textures/item/card_back.png"));
	private static final RenderType CARD_READY = RenderType
			.text(new ResourceLocation(Main.MODID, "textures/item/card_ready.png"));
	private static final RenderType CARD_BACK_FULL = RenderType
			.text(new ResourceLocation(Main.MODID, "textures/item/card_back_full.png"));

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
		renderCard(pose, CARD_FRONT, buffer, light, 32);
		if (card.canAttack()) {
			pose.pushPose();
			pose.translate(-1, -1, 0);
			renderCard(pose, CARD_READY, buffer, light, 34);
			pose.popPose();
		}
		pose.scale(-1, 1, 1);
		pose.translate(-32, 0, 0);
		renderCard(pose, card.getType() == null ? CARD_BACK_FULL : CARD_BACK, buffer, light, 32);
		pose.popPose();

		var type = card.getType();
		if (type == null)
			return;
		var entity = getEntity(card, mc.level);

		// Render title
		pose.pushPose();
		pose.translate(0.2, -0.065, 0.01);
		pose.scale(TITLE_SIZE, -TITLE_SIZE, TITLE_SIZE);
		mc.font.draw(pose, card.getName(), 0, 0, 0x000000);
		pose.popPose();

		// Render text
		for (int i = 0; i < DESCRIPTION_SIZES.length; i++) {
			var size = DESCRIPTION_SIZES[i];
			var lines = mc.font.split(card.getAbility().getDescription(), size.maxWidth);
			if (lines.size() <= size.maxLines || i == DESCRIPTION_SIZES.length - 1) {
				pose.pushPose();
				pose.translate(0.105, -0.53, 0.01);
				pose.scale(size.scale, -size.scale, size.scale);
				float y = 0;
				for (FormattedCharSequence line : lines) {
					mc.font.draw(pose, line, 0, y, 0);
					y += 9.5;
				}
				pose.popPose();
				break;
			}
		}

		// Render values
		var itemRenderer = mc.getItemRenderer();
		renderValue(itemRenderer, mc.font, Items.EMERALD, card.getCost(), 0.62f, -0.14f, light, overlay, pose, buffer);
		if (!card.isSpell()) {
			renderValue(itemRenderer, mc.font, Items.STONE_SWORD, card.getDamage(), 0.21f, -0.42f, light, overlay, pose,
					buffer);
			renderValue(itemRenderer, mc.font, Items.GLISTERING_MELON_SLICE, card.getHealth(), 0.6f, -0.42f, light,
					overlay, pose, buffer);
		}

		// Properties
		float propertyY = -0.08f;
		for (var entry : card.getProperties().entrySet()) {
			if (entry.getValue() > 0) {
				pose.pushPose();
				pose.translate(0.74, propertyY, 0);
				pose.scale(0.7f, 0.7f, 0.7f);
				if (entry.getValue() == 1) {
					renderItem(itemRenderer, entry.getKey().getIcon(), light, overlay, pose, buffer);
				} else {
					renderValue(itemRenderer, mc.font, entry.getKey().getIcon().getItem(), entry.getValue(), 0, 0,
							light, overlay, pose, buffer);
				}
				pose.popPose();
				propertyY -= 0.15f;
			}
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

		if (type == EntityType.ITEM) {
			pose.scale(3, 3, 3);
		}

		((EntityRenderer) mc.getEntityRenderDispatcher().getRenderer(entity)).render(entity, 0, 0, pose, buffer, light);
		pose.popPose();
	}

	private static void renderItem(ItemRenderer itemRenderer, ItemStack stack, int light, int overlay,
			PoseStack poseStack, MultiBufferSource buffer) {
		poseStack.pushPose();
		poseStack.scale(0.2f, 0.2f, 0.2f);
		itemRenderer.renderStatic(stack, TransformType.GUI, light, overlay, poseStack, buffer, 0);
		poseStack.popPose();
	}

	private static void renderValue(ItemRenderer itemRenderer, Font font, Item item, int value, float x, float y,
			int light, int overlay, PoseStack poseStack, MultiBufferSource buffer) {
		poseStack.pushPose();
		poseStack.translate(x, y, 0);

		// Item
		renderItem(itemRenderer, new ItemStack(item), light, overlay, poseStack, buffer);

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

		if (type == null)
			return null;

		// Find actual player
		if (type == EntityType.PLAYER) {
			if (card.getAdditionalData() instanceof AdditionalCardData.IdData idData) {
				for (var player : level.players())
					if (player.getUUID().equals(idData.getId()))
						return player;
			}
			return level.players().get(0);
		}

		var entity = CACHE.computeIfAbsent(type, t -> {
			// Special case for item to prevent spin
			if (t == EntityType.ITEM) {
				return new ItemEntity(EntityType.ITEM, level) {
					@Override
					public float getSpin(float partialTick) {
						return 0;
					}
				};
			}
			return t.create(level);
		});

		if (entity instanceof LivingEntity living) { // Change equipment
			for (var slot : EquipmentSlot.values()) {
				living.setItemSlot(slot, ItemStack.EMPTY);
			}
			for (var equipment : card.getEquipment().entrySet()) {
				living.setItemSlot(equipment.getKey(), equipment.getValue().getDefaultInstance());
			}
		} else if (entity instanceof ItemEntity itemEntity
				&& card.getAdditionalData() instanceof AdditionalCardData.ItemData itemData) { // Update item
			itemEntity.setExtendedLifetime();

			itemEntity.setItem(itemData.getItem().getDefaultInstance());
		}

		setSpecialAttributes(card, entity);

		return entity;
	}

	private static void setSpecialAttributes(Card card, Entity entity) {
		if (entity instanceof Rabbit rabbit) {
			rabbit.setRabbitType(card.hasProperty(CardProperty.SPECIAL) ? Rabbit.TYPE_EVIL : Rabbit.TYPE_BROWN);
		}
		if (entity instanceof Mob mob) {
			mob.setBaby(card.hasProperty(CardProperty.BABY));
		}
	}

	@Override
	public void renderByItem(ItemStack stack, TransformType transform, PoseStack pose, MultiBufferSource buffer,
			int light, int overlay) {

		if (!(stack.getItem() instanceof CardItem card))
			return;

		renderCard(Cards.getInstance(true).get(card.getType(stack)).getCardForRendering(), transform, pose, buffer,
				light, overlay);
	}

	private static void renderCard(PoseStack pose, RenderType card, MultiBufferSource buffer, int light, int size) {
		Matrix4f matrix = pose.last().pose();
		VertexConsumer consumer = buffer.getBuffer(card);
		consumer.vertex(matrix, 0, size, 0).color(255, 255, 255, 255).uv(0, 1).uv2(light).endVertex();
		consumer.vertex(matrix, size, size, 0).color(255, 255, 255, 255).uv(1, 1).uv2(light).endVertex();
		consumer.vertex(matrix, size, 0, 0).color(255, 255, 255, 255).uv(1, 0).uv2(light).endVertex();
		consumer.vertex(matrix, 0, 0, 0).color(255, 255, 255, 255).uv(0, 0).uv2(light).endVertex();
	}
}
