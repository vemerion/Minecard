package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.ability.CardAbilityGroup;
import mod.vemerion.minecard.game.ability.CardAbilityTrigger;
import mod.vemerion.minecard.game.ability.CardSelectionMethod;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.init.ModBlocks;
import mod.vemerion.minecard.init.ModCardAbilities;
import mod.vemerion.minecard.init.ModCardConditions;
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

		// Card properties
		add(CardProperty.CHARGE.getTextKey(), "charge");
		add(CardProperty.FREEZE.getTextKey(), "freeze");
		add(CardProperty.SHIELD.getTextKey(), "shield");
		add(CardProperty.STEALTH.getTextKey(), "stealth");
		add(CardProperty.TAUNT.getTextKey(), "taunt");

		// Card ability groups
		add(CardAbilityGroup.ALL.getTextKey(), "anywhere");
		add(CardAbilityGroup.SELF.getTextKey(), "self");
		add(CardAbilityGroup.TARGET.getTextKey(), "target");
		add(CardAbilityGroup.BOARD.getTextKey(), "the board");
		add(CardAbilityGroup.ENEMY_BOARD.getTextKey(), "the enemy board");
		add(CardAbilityGroup.YOUR_BOARD.getTextKey(), "your board");
		add(CardAbilityGroup.HANDS.getTextKey(), "all hands");
		add(CardAbilityGroup.ENEMY_HAND.getTextKey(), "the enemy hand");
		add(CardAbilityGroup.YOUR_HAND.getTextKey(), "your hand");
		add(CardAbilityGroup.ADJACENT.getTextKey(), "adjacent to this");

		// Card selection methods
		add(CardSelectionMethod.ALL.getTextKey(), "all cards from");
		add(CardSelectionMethod.RANDOM.getTextKey(), "a random card from");

		// Card conditions
		add(ModCardConditions.NO_CONDITION.get().getTranslationKey(), "");
		add(ModCardConditions.AND.get().getTranslationKey(), "(%s and %s)");
		add(ModCardConditions.OR.get().getTranslationKey(), "(%s or %s)");
		add(ModCardConditions.NOT.get().getTranslationKey(), "not (%s)");
		add(ModCardConditions.ENTITY.get().getTranslationKey(), "card is %s");

		// Card abilities triggers
		add(CardAbilityTrigger.ALWAYS.getTextKey(), "Always:");
		add(CardAbilityTrigger.NEVER.getTextKey(), "Never:");
		add(CardAbilityTrigger.SUMMON.getTextKey(), "Summon:");
		add(CardAbilityTrigger.ATTACK.getTextKey(), "Attack:");
		add(CardAbilityTrigger.DEATH.getTextKey(), "Death:");

		// Card abilities
		add(ModCardAbilities.NO_CARD_ABILITY.get().getTranslationKey(), "");
		add(ModCardAbilities.DRAW_CARDS.get().getTranslationKey(), "%s draw %s card(s).");
		add(ModCardAbilities.MODIFY.get().getTranslationKey(), "%s apply%s%s%s.");
		add(ModCardAbilities.MODIFY.get().getTranslationKey() + ".one_of", " one of");
		add(ModCardAbilities.MODIFY.get().getTranslationKey() + ".target", " to the target");
		add(ModCardAbilities.MODIFY.get().getTranslationKey() + ".element", " [%s%s/%s]");
		add(ModCardAbilities.ADD_CARDS.get().getTranslationKey(), "%s Add %s to your hand.");
		add(ModCardAbilities.RESOURCE.get().getTranslationKey(),
				"%s Gain %s temporary resources, and %s permanent resources.");
		add(ModCardAbilities.COPY_CARDS.get().getTranslationKey(), "%s copy %s %s%s%s.");
		add(Helper.gui("where"), " where ");
	}
}
