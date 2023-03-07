package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.ModelFile.UncheckedModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {

	public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
		super(generator, Main.MODID, existingFileHelper);
	}

	@Override
	protected void registerModels() {
		getBuilder(ModItems.CARD.get().getRegistryName().getPath())
				.parent(new UncheckedModelFile(mcLoc("builtin/entity")));

		var deckName = ModItems.DECK.get().getRegistryName().getPath();
		singleTexture(deckName, mcLoc(ITEM_FOLDER + "/generated"), "layer0", modLoc(folder + "/" + deckName));
	}

}
