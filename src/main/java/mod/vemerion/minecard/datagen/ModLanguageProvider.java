package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.ability.CardAbilityGroup;
import mod.vemerion.minecard.game.ability.CardAbilityTrigger;
import mod.vemerion.minecard.game.ability.CardPlacement;
import mod.vemerion.minecard.game.ability.CardSelectionMethod;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.init.ModBlocks;
import mod.vemerion.minecard.init.ModCardAbilities;
import mod.vemerion.minecard.init.ModCardConditions;
import mod.vemerion.minecard.init.ModCardOperators;
import mod.vemerion.minecard.init.ModCardVariables;
import mod.vemerion.minecard.init.ModEntities;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {

	private int tutorialCounter;

	public ModLanguageProvider(DataGenerator gen) {
		super(gen, Main.MODID, "en_us");
	}

	@Override
	protected void addTranslations() {
		add("itemGroup." + Main.MODID, "Minecard");
		add(ModItems.CARD.get(), "%s Card");
		add(ModItems.CARD.get().getDescriptionId() + ".tooltip_stats", "(%s/%s/%s)");
		add(ModItems.CARD.get().getDescriptionId() + ".tooltip_spell", "spell");
		add(ModItems.CARD.get().getDescriptionId() + ".tooltip_more", "Press shift to see description");
		add(ModItems.EMPTY_CARD_FRONT.get(), "Empty Card");
		add(ModItems.EMPTY_CARD_BACK.get(), "Empty Card");
		add(ModItems.EMPTY_CARD_FULL.get(), "Empty Card");

		add(ModBlocks.GAME.get(), "Game Board");
		add(ModItems.DECK.get(), "Deck");
		add(ModEntities.CARD_GAME_ROBOT.get(), "Card Player 9000");

		add("gui." + Main.MODID + ".game", "Minecard Game");

		add(Helper.gui("cardy"), "Cardy the Creeper");
		tutorial();

		add(Helper.chat("game_ongoing"), "A game is already ongoing.");
		add(Helper.chat("not_enough_cards"), "You need a full deck to enter the game.");
		add(Helper.chat("too_many_duplicates"), "Too many copies of %s in the deck (max = %s).");
		add(Helper.chat("game_interactions"),
				"Use [item] to interact with game:\n* Deck item: Enter the game\n* Redstone dust: Add an AI opponent to the game\n* Planks: Start a tutorial game");

		add(Helper.gui("next_turn"), "Next Turn");
		add(Helper.gui("game_over"), "Game Over");
		add(Helper.gui("resources_count"), "%s/%s");
		add(Helper.gui("deck_count"), "%s");
		add(Helper.gui("choose"), "Choose a card for ability:");
		add(Helper.gui("buried_treasure"), "Buried Treasure");
		add(Helper.gui("mulligan"), "Choose cards to mulligan");
		add(Helper.gui("confirm"), "Confirm");
		add(Helper.gui("info_button_hover"), "Click to cycle between pages");

		// Card properties
		add(CardProperty.getTextKey(CardProperty.CHARGE), "charge");
		add(CardProperty.getTextKey(CardProperty.FREEZE), "freeze");
		add(CardProperty.getTextKey(CardProperty.SHIELD), "shield");
		add(CardProperty.getTextKey(CardProperty.STEALTH), "stealth");
		add(CardProperty.getTextKey(CardProperty.TAUNT), "taunt");
		add(CardProperty.getTextKey(CardProperty.SPECIAL), "special");
		add(CardProperty.getTextKey(CardProperty.BABY), "baby");
		add(CardProperty.getTextKey(CardProperty.BURN), "burn");
		add(CardProperty.getTextKey(CardProperty.THORNS), "thorns");
		add(CardProperty.getTextKey(CardProperty.POISON), "poison");
		add(CardProperty.getTextKey(CardProperty.UNDEAD), "undead");

		add(CardProperty.getDescriptionKey(CardProperty.CHARGE), "Can attack immediately after being played");
		add(CardProperty.getDescriptionKey(CardProperty.FREEZE),
				"Decreases by 1 every turn, can not attack while frozen");
		add(CardProperty.getDescriptionKey(CardProperty.SHIELD), "Blocks first instance of damage");
		add(CardProperty.getDescriptionKey(CardProperty.STEALTH), "Can not be attacked until card has attacked");
		add(CardProperty.getDescriptionKey(CardProperty.TAUNT), "Must be killed before other cards can be attacked");
		add(CardProperty.getDescriptionKey(CardProperty.SPECIAL), "Special property depending on the creature");
		add(CardProperty.getDescriptionKey(CardProperty.BABY), "Grows up after X turns");
		add(CardProperty.getDescriptionKey(CardProperty.BURN), "Take 1 damage every turn until burn ends");
		add(CardProperty.getDescriptionKey(CardProperty.THORNS), "Deals extra damage when attacked");
		add(CardProperty.getDescriptionKey(CardProperty.POISON), "Take 1 damage every turn, but cannot kill");
		add(CardProperty.getDescriptionKey(CardProperty.UNDEAD), "The card belongs to the undead category");

		// Card ability groups
		add(CardAbilityGroup.ALL.getTextKey(), "anywhere");
		add(CardAbilityGroup.SELF.getTextKey(), "self");
		add(CardAbilityGroup.TARGET.getTextKey(), "target");
		add(CardAbilityGroup.ENEMY_DECK.getTextKey(), "the enemy deck");
		add(CardAbilityGroup.ENEMY_BOARD.getTextKey(), "the enemy board");
		add(CardAbilityGroup.YOUR_BOARD.getTextKey(), "your board");
		add(CardAbilityGroup.YOUR_DECK.getTextKey(), "your deck");
		add(CardAbilityGroup.ENEMY_HAND.getTextKey(), "the enemy hand");
		add(CardAbilityGroup.YOUR_HAND.getTextKey(), "your hand");
		add(CardAbilityGroup.ADJACENT.getTextKey(), "adjacent to this");
		add(CardAbilityGroup.TARGET_ADJACENT.getTextKey(), "adjacent to the target");

		// Card selection methods
		add(CardSelectionMethod.ALL.getTextKey(), "all cards from ");
		add(CardSelectionMethod.RANDOM.getTextKey(), "a random card from ");
		add(CardSelectionMethod.CHOICE.getTextKey(), "a selected card from ");

		// Card conditions
		add(ModCardConditions.NO_CONDITION.get().getTranslationKey(), "");
		add(ModCardConditions.AND.get().getTranslationKey(), "(%s and %s)");
		add(ModCardConditions.OR.get().getTranslationKey(), "(%s or %s)");
		add(ModCardConditions.NOT.get().getTranslationKey(), "not (%s)");
		add(ModCardConditions.ENTITY.get().getTranslationKey(), "card is %s");
		add(ModCardConditions.OPERATOR.get().getTranslationKey(), "%s");

		// Card variables
		add(ModCardVariables.COST.get().getTranslationKey(), "cost");
		add(ModCardVariables.DAMAGE.get().getTranslationKey(), "damage");
		add(ModCardVariables.HEALTH.get().getTranslationKey(), "health");
		add(ModCardVariables.MAX_HEALTH.get().getTranslationKey(), "max health");
		add(ModCardVariables.PROPERTY.get().getTranslationKey(), "%s");

		// Card Operators
		add(ModCardOperators.ADD.get().getTranslationKey(), "(%s + %s)");
		add(ModCardOperators.CONSTANT.get().getTranslationKey(), "%s");
		add(ModCardOperators.MUL.get().getTranslationKey(), "(%s * %s)");
		add(ModCardOperators.RANDOM.get().getTranslationKey(), "(value between %s and %s)");
		add(ModCardOperators.SUB.get().getTranslationKey(), "(%s - %s)");
		add(ModCardOperators.VARIABLE.get().getTranslationKey(), "%s");
		add(ModCardOperators.GREATER_THAN.get().getTranslationKey(), "(%s > %s)");

		// Card abilities triggers
		add(CardAbilityTrigger.SUMMON.getTextKey(), "Summon");
		add(CardAbilityTrigger.ATTACK.getTextKey(), "Attack");
		add(CardAbilityTrigger.DEATH.getTextKey(), "Death");
		add(CardAbilityTrigger.HURT.getTextKey(), "Hurt");
		add(CardAbilityTrigger.TICK.getTextKey(), "Tick");
		add(CardAbilityTrigger.GROW.getTextKey(), "Grow");

		// Card placements
		add(CardPlacement.LEFT.getTextKey(), "to the left");
		add(CardPlacement.RIGHT.getTextKey(), "to the right");
		add(CardPlacement.ENEMY.getTextKey(), "for the enemy");

		// Card abilities
		add(ModCardAbilities.NO_CARD_ABILITY.get().getTranslationKey(), "");
		add(ModCardAbilities.DRAW_CARDS.get().getTranslationKey(), "%s: draw %s card(s).");
		add(ModCardAbilities.MODIFY.get().getTranslationKey(), "%s: apply%s%s to %s.");
		add(ModCardAbilities.MODIFY.get().getTranslationKey() + ".one_of", " one of");
		add(ModCardAbilities.MODIFY.get().getTranslationKey() + ".element", " [%s]");
		add(ModCardAbilities.ADD_CARDS.get().getTranslationKey(), "%s: Receive %s.");
		add(ModCardAbilities.ADD_CARDS.get().getTranslationKey() + ".one_of", "one of");
		add(ModCardAbilities.ADD_CARDS.get().getTranslationKey() + ".element", " [%s]");

		add(ModCardAbilities.RESOURCE.get().getTranslationKey(),
				"%s: Gain %s temporary resources, and %s permanent resources.");
		add(ModCardAbilities.COPY_CARDS.get().getTranslationKey(), "%s: copy %s and give it to %s.%s%s");
		add(ModCardAbilities.COPY_CARDS.get().getTranslationKey() + ".destroy_original", " Destroy the original card.");
		add(ModCardAbilities.COPY_CARDS.get().getTranslationKey() + ".restore_health",
				" Restore the copy to full health.");
		add(ModCardAbilities.COPY_CARDS.get().getTranslationKey() + ".you", "you");
		add(ModCardAbilities.COPY_CARDS.get().getTranslationKey() + ".enemy", "the enemy");
		add(ModCardAbilities.SUMMON_CARD.get().getTranslationKey(), "%s: summon a %s %s.");
		add(ModCardAbilities.SUMMON_CARD.get().getTranslationKey() + "card_text", "%s (%s/%s)");
		add(ModCardAbilities.MULTI.get().getTranslationKey(), "%s");
		add(ModCardAbilities.CHANCE.get().getTranslationKey(), "%s%% %s: %s");
		add(ModCardAbilities.CHANCE.get().getTranslationKey() + ".chance", "chance");
		add(ModCardAbilities.CHOICE.get().getTranslationKey(), "%s:\n%s");
		add(ModCardAbilities.CHOICE.get().getTranslationKey() + ".choice_of", "Choice of");
		add(ModCardAbilities.GAME_OVER.get().getTranslationKey(), "%s: lose the game");
		add(Helper.gui("card_ability_selection"), "%s%s%s%s");
		add(Helper.gui("where"), " where ");
		add(Helper.gui("if"), " if ");
		add(Helper.gui("card_modification"), "%s %s %s");
		add(Helper.gui("equal"), "=");
		add(Helper.gui("plus_equal"), "+=");
	}

	private void tutorial() {
		addTutorialStep(
				"Hello there! My name is Cardy the Creeper and I am here to teach you about the card game. First I will teach you about the cards themselves!");
		addTutorialStep(
				"Also! If I get in the way, you can easily drag me around with the mouse. But be careful, because I am ticklish!");
		addTutorialStep("Oh look, that's me! Wait, something seems off..");
		addTutorialStep("There, much better. Now, let the teaching begin!");
		addTutorialStep("This value determines how much it costs to play the card.");
		addTutorialStep("This value determines how much damage the card can take before it dies.");
		addTutorialStep("This value determines how much damage the card does when it attacks.");
		addTutorialStep(
				"The column to the right of the card lists all the special properties a card has. For example, this card has thorns, so it will deal extra damage when attacked.");
		addTutorialStep(
				"You can hover over this button at any time during the game to see a list of all the different properties.");
		addTutorialStep(
				"This area describes any abilities the card has. Abilities have a trigger and an effect. In this case, the trigger is 'attack', which means the effect will happen every time the card attacks.");
		addTutorialStep("Enough about the cards! Let's move on to the rest of the game.");
		addTutorialStep("This bottom half is your part of the game space.");
		addTutorialStep(
				"This is your deck. At the start of your turn, you automatically draw one card from your deck.");
		addTutorialStep("This is your hand.");
		addTutorialStep(
				"The game always starts with a mulligan phase, where you get to select which cards you want to replace from the starting hand.");
		addTutorialStep(
				"All the cards in the tutorial deck are identical, so it does not matter if you replace cards or not.");
		addTutorialStep("Click on the 'Confirm' button to complete the mulligan phase and continue with the tutorial.");
		addTutorialStep("This is your part of the board.");
		addTutorialStep(
				"Each player always start with one card on the board, the player card. When you kill the opponent player card, you win the game.");
		addTutorialStep(
				"Usually the player card has %s health, but I have reduced it to 1 so the tutorial won't drag on for too long!");
		addTutorialStep(
				"Here are your resources. These are drained when you play cards. When a new turn starts your resources are restored, and you gain 1 more total.");
		addTutorialStep(
				"Let's try playing a card! Click on one of the cards in your hand, and then click somewhere on your board.");
		addTutorialStep(
				"Great work! As you might have noticed, an activity entry was added here to the left when you played a card.");
		addTutorialStep(
				"Every time you or the opponent plays a card, attacks, or a card ability comes into effect, an entry is added, which you can use to see if you missed anything important.");
		addTutorialStep("If you hover over an entry, you will get the details of what happened.");
		addTutorialStep(
				"Now, back to the game at hand! Unfortunately, played cards need one turn to get ready before they can attack, so not much more to do than to end the turn.");
		addTutorialStep("Click on the end turn button!");
		addTutorialStep(
				"Usually this is when the opponent will make their move, but fortunately for you, the AI is programmed to just pass their turn during the tutorial.");
		addTutorialStep(
				"Now your card is ready to attack! Click on it, and then click on the enemy card to attack it and end the tutorial!");
		addTutorialStep(
				"Good job! You have completed the tutorial! Now get out there and collect some cards and play against new opponents :)");
	}

	private void addTutorialStep(String text) {
		add(Helper.tutorial(tutorialCounter), text);
		tutorialCounter++;
	}
}
