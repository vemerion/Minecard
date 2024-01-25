package mod.vemerion.minecard.renderer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.entity.CardGameRobot;
import mod.vemerion.minecard.game.AIPlayer;
import mod.vemerion.minecard.game.AdditionalCardData;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardProperties;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.init.ModEntities;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.TransformationHelper;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class CardItemRenderer extends BlockEntityWithoutLevelRenderer {

	private static record DescriptionSize(float scale, int maxWidth, int maxLines) {
	}

	private static final DescriptionSize[] DESCRIPTION_SIZES = { new DescriptionSize(0.007f, 79, 3),
			new DescriptionSize(0.006f, 90, 4), new DescriptionSize(0.005f, 110, 5),
			new DescriptionSize(0.004f, 135, 6) };

	private static final Map<EntityType<?>, Entity> CACHE = new HashMap<>();

	private static final float TEXT_SIZE = 0.01f;
	private static final float TITLE_SIZE = 0.006f;
	private static final float TITLE_MAX_WIDTH = 82;
	private static final float CARD_SIZE = 0.8f;
	public static final int GOOD_VALUE_COLOR = 0x00ff70;
	public static final int BAD_VALUE_COLOR = 0xff2020;
	public static final int NEUTRAL_VALUE_COLOR = 0xffffff;

	public CardItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
		super(dispatcher, modelSet);
	}

	public static CardGameRobot getRobot(Level level) {
		return (CardGameRobot) CACHE.computeIfAbsent(ModEntities.CARD_GAME_ROBOT.get(), t -> t.create(level));
	}

	public static void renderCard(Card card, ItemDisplayContext transform, PoseStack pose, MultiBufferSource buffer,
			int light, int overlay) {
		Lighting.setupForFlatItems();
		var mc = Minecraft.getInstance();
		var itemRenderer = mc.getItemRenderer();

		// Render card
		if (transform == ItemDisplayContext.GUI)
			pose.translate(0.01, -0.1, 0);
		if (transform != ItemDisplayContext.NONE)
			pose.translate(0.1, 1, 0.45);
		pose.pushPose();
		pose.translate(0.4, -0.4, -0.03);
		pose.scale(CARD_SIZE, CARD_SIZE, CARD_SIZE);
		itemRenderer.renderStatic(ModItems.EMPTY_CARD_FRONT.get().getDefaultInstance(), ItemDisplayContext.NONE, light,
				overlay, pose, buffer, null, 0);
		pose.translate(-0.001, -0.001, -0.001);
		itemRenderer.renderStatic(
				card.getType().isEmpty() ? ModItems.EMPTY_CARD_FULL.get().getDefaultInstance()
						: ModItems.EMPTY_CARD_BACK.get().getDefaultInstance(),
				ItemDisplayContext.NONE, light, overlay, pose, buffer, null, 0);
		pose.popPose();

		var type = card.getType();
		if (type.isEmpty())
			return;
		var entity = getEntity(card, mc.level);

		// Render title
		pose.pushPose();
		var name = entity.getDisplayName();
		float titleSize = Math.min(1f, TITLE_MAX_WIDTH / mc.font.width(name)) * TITLE_SIZE;
		pose.translate(0.124 + (0.555 - mc.font.width(name) * titleSize) / 2f,
				-0.092 + mc.font.lineHeight * titleSize / 2, 0.01);
		pose.scale(titleSize, -titleSize, titleSize);
		mc.font.draw(pose, name, 0, 0, 0);
		pose.popPose();

		// Render text
		for (int i = 0; i < DESCRIPTION_SIZES.length; i++) {
			var size = DESCRIPTION_SIZES[i];
			var lines = mc.font.split(card.getAbility().getText(), size.maxWidth);
			if (lines.size() <= size.maxLines || i == DESCRIPTION_SIZES.length - 1) {
				int start = 0;
				var tooLong = i == DESCRIPTION_SIZES.length - 1 && lines.size() > size.maxLines;
				if (tooLong) {
					var max = (lines.size() - size.maxLines + 1);
					start = (card.getTextScroll() % max + max) % max;
				}
				pose.pushPose();
				pose.translate(0.131, -0.521, 0.01);
				pose.scale(size.scale, -size.scale, size.scale);
				float y = 0;
				int end = Math.min(start + size.maxLines, lines.size());
				for (int j = start; j < end; j++) {
					if (j == end - 1 && j != lines.size() - 1) {
						mc.font.draw(pose, "[ ... ]", 0, y, 0);
					} else {
						mc.font.draw(pose, lines.get(j), 0, y, 0);
					}
					y += 9.5;
				}
				pose.popPose();
				break;
			}
		}

		// Render values
		renderValue(itemRenderer, mc.font, Items.EMERALD.getDefaultInstance(), card.getCost(), 0.62f, -0.195f, light,
				overlay, pose, buffer, calcCostColor(card));
		if (!card.isSpell()) {
			pose.pushPose();
			if (card.canAttack()) {
				float scale = Mth.sin((mc.level.getGameTime() % 100000 + mc.getFrameTime()) / 10) * 0.2f + 1;
				pose.translate(0.2, -0.5, 0);
				pose.scale(scale, scale, 1);
				pose.translate(-0.2, 0.5, 0);
			}
			renderValue(itemRenderer, mc.font, Items.STONE_SWORD.getDefaultInstance(), card.getDamage(), 0.21f, -0.41f,
					light, overlay, pose, buffer, calcDamageColor(card));
			pose.popPose();
			renderValue(itemRenderer, mc.font, Items.GLISTERING_MELON_SLICE.getDefaultInstance(), card.getHealth(),
					0.6f, -0.42f, light, overlay, pose, buffer, calcHealthColor(card));
		}

		// Properties
		float propertyY = -0.08f;
		for (var entry : card.getProperties().entrySet()) {
			if (entry.getValue() > 0) {
				pose.pushPose();
				pose.translate(0.74, propertyY, 0);
				pose.scale(0.7f, 0.7f, 0.7f);
				var property = CardProperties.getInstance(true).get(entry.getKey());
				if (property != null && !property.getItem().isEmpty()) {
					if (entry.getValue() == 1) {
						renderItem(itemRenderer, property.getItem(), light, overlay, pose, buffer);
					} else {
						renderValue(itemRenderer, mc.font, property.getItem(), entry.getValue(), 0, 0, light, overlay,
								pose, buffer, 0xffffff);
					}
					propertyY -= 0.15f;
				}
				pose.popPose();
			}
		}

		// Render entity
		float maxWidth = 2;
		float maxHeight = 2.3f;

		var dimensions = entity.getType().getDimensions();
		var widthScale = Math.min(1, maxWidth / dimensions.width);
		var heightScale = Math.min(1, maxHeight / dimensions.height);
		var scale = Math.min(widthScale, heightScale);
		var scaleFactor = entity.getType() == EntityType.ITEM ? 3 : 1;
		var drawingScale = scale * scaleFactor * 0.15f;

		if (entity.getType() == EntityType.ITEM) {
			scale *= 3;
		}

		pose.pushPose();
		pose.translate(0.4, -0.47 + (0.34f - dimensions.height * drawingScale * scaleFactor) / 2f, 0);
		if (transform == ItemDisplayContext.NONE)
			pose.translate(0, 0, -0.2);
		pose.scale(drawingScale, drawingScale, drawingScale);
		pose.mulPose(TransformationHelper.quatFromXYZ(0, 20, 0, true));

		if (entity instanceof LivingEntity living) {
			var xRotSaved = living.getXRot();
			var xRot0Saved = living.xRotO;
			var yBodyRotSaved = living.yBodyRot;
			var yBodyRot0Saved = living.yBodyRotO;
			var yHeadRotSaved = living.yHeadRot;
			var yHeadRot0Saved = living.yHeadRot;
			living.setXRot(0);
			living.xRotO = living.getXRot();
			living.yBodyRot = 0;
			living.yBodyRotO = living.yBodyRot;
			living.yHeadRot = 0;
			living.yHeadRotO = living.yHeadRot;
			mc.getEntityRenderDispatcher().getRenderer(entity).render(entity, 0, 0, pose, buffer, light);
			living.setXRot(xRotSaved);
			living.xRotO = xRot0Saved;
			living.yBodyRot = yBodyRotSaved;
			living.yBodyRotO = yBodyRot0Saved;
			living.yHeadRot = yHeadRotSaved;
			living.yHeadRotO = yHeadRot0Saved;
		} else {
			mc.getEntityRenderDispatcher().getRenderer(entity).render(entity, 0, 0, pose, buffer, light);
		}
		pose.popPose();
	}

	private static int calcCostColor(Card card) {
		if (card.getCost() > card.getOriginalCost())
			return BAD_VALUE_COLOR;
		else if (card.getCost() == card.getOriginalCost())
			return NEUTRAL_VALUE_COLOR;
		return GOOD_VALUE_COLOR;
	}

	private static int calcDamageColor(Card card) {
		if (card.getDamage() < card.getOriginalDamage())
			return BAD_VALUE_COLOR;
		else if (card.getDamage() == card.getOriginalDamage())
			return NEUTRAL_VALUE_COLOR;
		return GOOD_VALUE_COLOR;
	}

	private static int calcHealthColor(Card card) {
		if (card.getHealth() < card.getMaxHealth())
			return BAD_VALUE_COLOR;
		else if (card.getMaxHealth() <= card.getOriginalHealth())
			return NEUTRAL_VALUE_COLOR;
		return GOOD_VALUE_COLOR;
	}

	private static void renderItem(ItemRenderer itemRenderer, ItemStack stack, int light, int overlay,
			PoseStack poseStack, MultiBufferSource buffer) {
		poseStack.pushPose();
		poseStack.scale(0.2f, 0.2f, 0.2f);
		itemRenderer.renderStatic(stack, ItemDisplayContext.GUI, light, overlay, poseStack, buffer, null, 0);
		poseStack.popPose();
	}

	private static void renderValue(ItemRenderer itemRenderer, Font font, ItemStack stack, int value, float x, float y,
			int light, int overlay, PoseStack poseStack, MultiBufferSource buffer, int textColor) {
		poseStack.pushPose();
		poseStack.translate(x, y, 0);

		// Item
		renderItem(itemRenderer, stack, light, overlay, poseStack, buffer);

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
		font.draw(poseStack, String.valueOf(value), 0, 0, textColor);
		poseStack.popPose();
	}

	public static Entity getEntity(Card card, ClientLevel level) {
		var type = card.getType();

		if (type.isEmpty())
			return null;

		// Find actual player
		if (type.get() == EntityType.PLAYER) {
			if (card.getAdditionalData() instanceof AdditionalCardData.IdData idData) {
				var id = idData.getId();
				if (AIPlayer.isAi(id)) {
					return CACHE.computeIfAbsent(ModEntities.CARD_GAME_ROBOT.get(), t -> t.create(level));
				}

				for (var player : level.players())
					if (player.getUUID().equals(id))
						return player;
			}
			return level.players().get(0);
		}

		var entity = CACHE.computeIfAbsent(type.get(), t -> {
			// Special case for item to prevent spin
			if (t == EntityType.ITEM) {
				return new ItemEntity(EntityType.ITEM, level) {
					@Override
					public float getSpin(float partialTick) {
						return 0;
					}
				};
			} else if (t == EntityType.TROPICAL_FISH) {
				return new TropicalFish(EntityType.TROPICAL_FISH, level) {
					@Override
					public boolean isInWater() {
						return true;
					}
				};
			}
			return t.create(level);
		});

		if (entity instanceof ItemEntity itemEntity
				&& card.getAdditionalData() instanceof AdditionalCardData.ItemData itemData) { // Update item
			itemEntity.setExtendedLifetime();

			itemEntity.setItem(itemData.getStack());
		}

		setSpecialAttributes(card, entity);

		return entity;
	}

	private static final Method SET_PACKED_VARIANT = ObfuscationReflectionHelper.findMethod(TropicalFish.class,
			"m_30056_", int.class);

	private static void setSpecialAttributes(Card card, Entity entity) {
		if (entity instanceof Rabbit rabbit) {
			rabbit.setVariant(card.hasProperty(CardProperty.SPECIAL) ? Rabbit.Variant.EVIL : Rabbit.Variant.BROWN);
		}
		if (entity instanceof Mob mob) {
			mob.setBaby(card.hasProperty(CardProperty.BABY));
		}
		if (entity instanceof TropicalFish fish) {
			var rand = new Random(card.getId());
			try {
				SET_PACKED_VARIANT.invoke(fish,
						new TropicalFish.Variant(
								TropicalFish.Pattern.values()[rand.nextInt(TropicalFish.Pattern.values().length)],
								DyeColor.values()[rand.nextInt(DyeColor.values().length)],
								DyeColor.values()[rand.nextInt(DyeColor.values().length)]).getPackedId());
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				Main.LOGGER.error("Unable to play set variant for tropical fish card");
			}
		}
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext transform, PoseStack pose, MultiBufferSource buffer,
			int light, int overlay) {

		CardData.getType(stack).ifPresent(rl -> {
			var card = Cards.getInstance(true).get(rl);
			if (card != null)
				renderCard(card.getCardForRendering(), transform, pose, buffer, light, overlay);
		});
	}
}
