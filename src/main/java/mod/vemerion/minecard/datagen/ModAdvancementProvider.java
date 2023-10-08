package mod.vemerion.minecard.datagen;

import java.util.List;
import java.util.function.Consumer;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.advancement.ModCollectCardTrigger;
import mod.vemerion.minecard.advancement.ModFinishGameTrigger;
import mod.vemerion.minecard.advancement.ModGameTrigger;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.ForgeAdvancementProvider.AdvancementGenerator;
import net.minecraftforge.registries.ForgeRegistries;

public class ModAdvancementProvider implements AdvancementGenerator {

	private static final List<EntityType<?>> BOSSES = List.of(EntityType.ENDER_DRAGON, EntityType.WITHER);
	private static final List<EntityType<?>> MOBS = List.of(EntityType.SNOW_GOLEM, EntityType.WITHER_SKELETON,
			EntityType.VILLAGER, EntityType.PHANTOM, EntityType.ELDER_GUARDIAN, EntityType.SHULKER,
			EntityType.ENDERMITE, EntityType.DONKEY, EntityType.WOLF, EntityType.STRAY, EntityType.LLAMA,
			EntityType.STRIDER, EntityType.CAVE_SPIDER, EntityType.WITCH, EntityType.PILLAGER, EntityType.PIGLIN_BRUTE,
			EntityType.ZOMBIE, EntityType.CAT, EntityType.VINDICATOR, EntityType.MULE, EntityType.SHEEP,
			EntityType.CREEPER, EntityType.SPIDER, EntityType.SKELETON_HORSE, EntityType.PIG, EntityType.TRADER_LLAMA,
			EntityType.GOAT, EntityType.FOX, EntityType.HORSE, EntityType.ENDERMAN, EntityType.BLAZE, EntityType.SALMON,
			EntityType.MAGMA_CUBE, EntityType.GLOW_SQUID, EntityType.ZOGLIN, EntityType.BEE, EntityType.CHICKEN,
			EntityType.AXOLOTL, EntityType.MOOSHROOM, EntityType.DROWNED, EntityType.IRON_GOLEM, EntityType.SQUID,
			EntityType.COW, EntityType.DOLPHIN, EntityType.ZOMBIE_VILLAGER, EntityType.PIGLIN, EntityType.VEX,
			EntityType.HOGLIN, EntityType.SLIME, EntityType.RABBIT, EntityType.PUFFERFISH, EntityType.OCELOT,
			EntityType.GHAST, EntityType.TURTLE, EntityType.WANDERING_TRADER, EntityType.POLAR_BEAR, EntityType.RAVAGER,
			EntityType.HUSK, EntityType.GUARDIAN, EntityType.SKELETON, EntityType.SILVERFISH, EntityType.EVOKER,
			EntityType.TROPICAL_FISH, EntityType.PARROT, EntityType.COD, EntityType.BAT, EntityType.PANDA,
			EntityType.ZOMBIFIED_PIGLIN);

	@Override
	public void generate(HolderLookup.Provider registries, Consumer<Advancement> consumer,
			ExistingFileHelper fileHelper) {
		var root = Advancement.Builder.advancement()
				.display(ModItems.EMPTY_CARD_FULL.get(), title("root"), description("root"),
						new ResourceLocation(Main.MODID, "textures/block/game_top.png"), FrameType.TASK, true, true,
						false)
				.addCriterion("has_card", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.CARD.get()))
				.save(consumer, path("root"));
		Advancement.Builder.advancement().parent(root)
				.display(ModItems.GAME.get(), title("win"), description("win"), null, FrameType.TASK, true, true, false)
				.addCriterion("won_game", ModFinishGameTrigger.Instance.create(ModFinishGameTrigger.Type.WIN_GAME))
				.save(consumer, path("win"));
		Advancement.Builder.advancement().parent(root)
				.display(Items.BOOK, title("tutorial"), description("tutorial"), null, FrameType.TASK, true, true,
						false)
				.addCriterion("tutorial",
						ModFinishGameTrigger.Instance.create(ModFinishGameTrigger.Type.COMPLETE_TUTORIAL))
				.save(consumer, path("tutorial"));
		Advancement.Builder.advancement().parent(root)
				.display(Items.REDSTONE, title("win_ai"), description("win_ai"), null, FrameType.TASK, true, true,
						false)
				.addCriterion("won_agains_ai", ModFinishGameTrigger.Instance.create(ModFinishGameTrigger.Type.WIN_AI))
				.save(consumer, path("win_ai"));

		Advancement.Builder.advancement().parent(root)
				.display(Items.ZOMBIE_HEAD, title("zombie_buff"), description("zombie_buff"), null, FrameType.GOAL,
						true, true, false)
				.addCriterion("zombie_buff",
						ModGameTrigger.Instance.create(new ResourceLocation(Main.MODID, "zombie_buff")))
				.save(consumer, path("zombie_buff"));
		Advancement.Builder.advancement().parent(root)
				.display(Items.WITHER_SKELETON_SKULL, title("discount_wither"), description("discount_wither"), null,
						FrameType.GOAL, true, true, false)
				.addCriterion("discount_wither",
						ModGameTrigger.Instance.create(new ResourceLocation(Main.MODID, "discount_wither")))
				.save(consumer, path("discount_wither"));
		Advancement.Builder.advancement().parent(root)
				.display(Raid.getLeaderBannerInstance(), title("sweeping_edge"), description("sweeping_edge"), null,
						FrameType.GOAL, true, true, false)
				.addCriterion("sweeping_edge",
						ModGameTrigger.Instance.create(new ResourceLocation(Main.MODID, "sweeping_edge")))
				.save(consumer, path("sweeping_edge"));
		Advancement.Builder.advancement().parent(root)
				.display(Items.IRON_INGOT, title("iron_golem_farm"), description("iron_golem_farm"), null,
						FrameType.GOAL, true, true, false)
				.addCriterion("iron_golem_farm",
						ModGameTrigger.Instance.create(new ResourceLocation(Main.MODID, "iron_golem_farm")))
				.save(consumer, path("iron_golem_farm"));

		collect(Advancement.Builder.advancement(), BOSSES.stream().map(ForgeRegistries.ENTITY_TYPES::getKey).toList())
				.parent(root).display(Items.DRAGON_HEAD, title("collect_boss"), description("collect_boss"), null,
						FrameType.GOAL, true, true, false)
				.requirements(RequirementsStrategy.OR).save(consumer, path("collect_boss"));
		collect(Advancement.Builder.advancement(), Cards.SPELLS).parent(root).display(Items.CHEST,
				title("collect_spells"), description("collect_spells"), null, FrameType.CHALLENGE, true, true, false)
				.save(consumer, path("collect_spells"));
		collect(Advancement.Builder.advancement(), MOBS.stream().map(ForgeRegistries.ENTITY_TYPES::getKey).toList())
				.parent(root).display(Items.EGG, title("collect_mobs"), description("collect_mobs"), null,
						FrameType.CHALLENGE, true, true, false)
				.save(consumer, path("collect_mobs"));
	}

	private Advancement.Builder collect(Advancement.Builder builder, Iterable<ResourceLocation> cards) {
		for (var c : cards) {
			builder.addCriterion("has_" + c.toString(), ModCollectCardTrigger.Instance.create(c));
		}
		return builder;
	}

	private Component title(String s) {
		return Component.translatable(titleKey(s));
	}

	private Component description(String s) {
		return Component.translatable(descriptionKey(s));
	}

	public static String titleKey(String s) {
		return "advancements." + Main.MODID + "." + s + ".title";
	}

	public static String descriptionKey(String s) {
		return "advancements." + Main.MODID + "." + s + ".description";
	}

	private String path(String s) {
		return Main.MODID + "/" + s;
	}
}
