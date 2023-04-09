package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.ability.CardAbilityTrigger;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.init.ModBlocks;
import mod.vemerion.minecard.init.ModCardAbilities;
import mod.vemerion.minecard.init.ModItems;
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

		add(ModBlocks.GAME.get(), "Game Board");
		add(ModItems.DECK.get(), "Deck");

		add("gui." + Main.MODID + ".game", "Minecard Game");

		add(Helper.chat("not_enough_players"), "Need one more player to start the game.");
		add(Helper.chat("game_ongoing"), "A game is already ongoing.");
		add(Helper.chat("need_deck"), "Right-click with a deck to enter the game.");
		add(Helper.chat("not_enough_cards"), "You need a full deck to enter the game.");

		add(Helper.gui("your_turn"), "Your Turn");
		add(Helper.gui("enemy_turn"), "Enemy Turn");
		add(Helper.gui("next_turn"), "Next Turn");
		add(Helper.gui("game_over"), "Game Over");
		add(Helper.gui("resources_count"), "%s/%s");
		add(Helper.gui("deck_count"), "%s");

		// Card abilities
		add(CardAbilityTrigger.ALWAYS.getTextKey(), "Always:");
		add(CardAbilityTrigger.NEVER.getTextKey(), "Never:");
		add(CardAbilityTrigger.SUMMON.getTextKey(), "Summon:");

		add(ModCardAbilities.NO_CARD_ABILITY.get().getTranslationKey(), "");
		add(ModCardAbilities.DRAW_CARDS.get().getTranslationKey(), "%s draw %s card(s).");
	}
}
