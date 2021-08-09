package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.eventsubscriber.ModEventSubscriber;
import mod.vemerion.minecard.item.CardItem;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

	public ModLanguageProvider(DataGenerator gen) {
		super(gen, Main.MODID, "en_us");
	}

	// @formatter:off
	@Override
	protected void addTranslations() {
		for (CardItem card : ModEventSubscriber.getCards())
			add(card, "Card: " + card.getEntityName());
	}
}
