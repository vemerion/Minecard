package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.eventsubscriber.ModEventSubscriber;
import mod.vemerion.minecard.item.CardItem;
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
		for (CardItem card : ModEventSubscriber.getCards())
			getBuilder(card.getRegistryName().getPath()).parent(new UncheckedModelFile(mcLoc("builtin/entity")));
	}

}
