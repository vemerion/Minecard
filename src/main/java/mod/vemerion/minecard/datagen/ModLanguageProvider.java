package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.init.ModBlocks;
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
		add("itemGroup." + Main.MODID, "Minecard");
		add(ModItems.CARD.get(), "%s Card");
		addCardText(ModItems.CARD.get(), "Use to blow up!");

		add(ModBlocks.GAME.get(), "Game Board");
		add(ModItems.DECK.get(), "Deck");

		add("gui." + Main.MODID + ".game", "Minecard Game");

		add(Helper.chat("not_enough_players"), "Need one more player to start the game.");
		add(Helper.chat("game_ongoing"), "A game is already ongoing.");
		add(Helper.chat("need_deck"), "Right-click with a deck to enter the game.");
		add(Helper.chat("not_enough_cards"), "You need a full deck to enter the game.");
		
		add(Helper.gui("your_turn"), "Your Turn");
		add(Helper.gui("enemy_turn"), "Enemy Turn");
	}

	private void addCardText(CardItem card, String descr) {
		add(card.getCardTextId(), descr);
	}
}
