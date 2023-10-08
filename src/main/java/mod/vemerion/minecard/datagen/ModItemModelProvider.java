package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ModItemModelProvider extends ItemModelProvider {

	public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
		super(output, Main.MODID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		getBuilder(ForgeRegistries.ITEMS.getKey(ModItems.CARD.get()).getPath())
				.parent(new UncheckedModelFile(mcLoc("builtin/entity")));

		simple(ModItems.DECK.get());
		simple(ModItems.EMPTY_CARD_FRONT.get());
		simple(ModItems.EMPTY_CARD_BACK.get());
		simple(ModItems.EMPTY_CARD_FULL.get());
	}

	private void simple(Item item) {
		var deckName = ForgeRegistries.ITEMS.getKey(item).getPath();
		singleTexture(deckName, mcLoc(ITEM_FOLDER + "/generated"), "layer0", modLoc(folder + "/" + deckName));

	}

}
