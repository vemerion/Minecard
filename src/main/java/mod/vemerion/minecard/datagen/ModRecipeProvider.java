package mod.vemerion.minecard.datagen;

import java.util.function.Consumer;

import mod.vemerion.minecard.init.ModBlocks;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraftforge.common.Tags;

public class ModRecipeProvider extends RecipeProvider {

	public ModRecipeProvider(PackOutput pOutput) {
		super(pOutput);
	}

	@Override
	protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GAME.get()).define('#', ItemTags.PLANKS)
				.define('C', ModItems.CARD.get()).pattern("#C#").pattern(" # ").pattern("###")
				.unlockedBy("has_card", has(ModItems.CARD.get())).save(consumer);
		ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DECK.get()).define('#', Tags.Items.LEATHER)
				.define('C', ModItems.CARD.get()).pattern("###").pattern("#C#").pattern("###")
				.unlockedBy("has_card", has(ModItems.CARD.get())).save(consumer);
	}
}
