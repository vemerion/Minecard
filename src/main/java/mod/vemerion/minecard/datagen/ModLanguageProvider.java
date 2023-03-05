package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.init.ModItems;
import mod.vemerion.minecard.item.CardItem;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

	public ModLanguageProvider(DataGenerator gen) {
		super(gen, Main.MODID, "en_us");
	}

	@Override
	protected void addTranslations() {
		add(ModItems.CARD.get(), "Card");
		addCardText(ModItems.CARD.get(), "Use to blow up!");
	}

	private void addCardText(CardItem card, String descr) {
		add(card.getCardTextId(), descr);
	}
}
