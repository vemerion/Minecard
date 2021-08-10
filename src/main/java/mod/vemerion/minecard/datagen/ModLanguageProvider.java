package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Cards;
import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.item.CardItem;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

	public ModLanguageProvider(DataGenerator gen) {
		super(gen, Main.MODID, "en_us");
	}

	@Override
	protected void addTranslations() {
		add(Cards.CREEPER_CARD, "Card: Creeper");
		addCardText(Cards.CREEPER_CARD, "Use to blow up!");
	}

	private void addCardText(CardItem card, String descr) {
		add(card.getCardTextId(), descr);
	}
}
