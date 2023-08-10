package mod.vemerion.minecard.datagen;

import java.util.function.Consumer;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.advancement.ModFinishGameTrigger;
import mod.vemerion.minecard.advancement.ModGameTrigger;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModAdvancementProvider extends AdvancementProvider {

	public ModAdvancementProvider(DataGenerator generatorIn, ExistingFileHelper fileHelperIn) {
		super(generatorIn, fileHelperIn);
	}

	@Override
	protected void registerAdvancements(Consumer<Advancement> consumer, ExistingFileHelper fileHelper) {
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
	}

	private TranslatableComponent title(String s) {
		return new TranslatableComponent(titleKey(s));
	}

	private TranslatableComponent description(String s) {
		return new TranslatableComponent(descriptionKey(s));
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

	@Override
	public String getName() {
		return Main.MODID + ": Advancements";
	}
}
