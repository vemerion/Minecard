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
import mod.vemerion.minecard.init.ModEntities;
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
		add(ModEntities.CARD_GAME_ROBOT.get(), "Card Player 9000");

		add("gui." + Main.MODID + ".game", "Minecard Game");

		add(Helper.chat("game_ongoing"), "A game is already ongoing.");
		add(Helper.chat("not_enough_cards"), "You need a full deck to enter the game.");
		add(Helper.chat("game_interactions"),
				"Use [item] to interact with game:\n* Deck item: Enter the game\n* Redstone dust: Add an AI opponent to the game");

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
		add(CardProperty.SPECIAL.getTextKey(), "special");
		add(CardProperty.BABY.getTextKey(), "baby");
		add(CardProperty.BURN.getTextKey(), "burn");
		add(CardProperty.THORNS.getTextKey(), "thorns");

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
		add(CardAbilityTrigger.HURT.getTextKey(), "Hurt:");
		add(CardAbilityTrigger.TICK.getTextKey(), "Tick:");
		add(CardAbilityTrigger.GROW.getTextKey(), "Grow:");

		// Card placements
		add(CardPlacement.LEFT.getTextKey(), "to the left");
		add(CardPlacement.RIGHT.getTextKey(), "to the right");
		add(CardPlacement.ENEMY.getTextKey(), "for the enemy");

		// Card abilities
		add(ModCardAbilities.NO_CARD_ABILITY.get().getTranslationKey(), "");
		add(ModCardAbilities.DRAW_CARDS.get().getTranslationKey(), "%s draw %s card(s).");
		add(ModCardAbilities.MODIFY.get().getTranslationKey(), "%s apply%s%s to %s.");
		add(ModCardAbilities.MODIFY.get().getTranslationKey() + ".one_of", " one of");
		add(ModCardAbilities.MODIFY.get().getTranslationKey() + ".element", " [%s%s/%s%s%s]");
		add(ModCardAbilities.MODIFY.get().getTranslationKey() + ".element_cost", ", cost %s");
		add(ModCardAbilities.MODIFY.get().getTranslationKey() + ".element_heal", ", restoring %s health");
		add(ModCardAbilities.MODIFY.get().getTranslationKey() + ".element_hurt", ", taking %s damage");
		add(ModCardAbilities.ADD_CARDS.get().getTranslationKey(), "%s Add %s to your hand.");
		add(ModCardAbilities.RESOURCE.get().getTranslationKey(),
				"%s Gain %s temporary resources, and %s permanent resources.");
		add(ModCardAbilities.COPY_CARDS.get().getTranslationKey(), "%s copy %s and give it to %s.%s%s");
		add(ModCardAbilities.COPY_CARDS.get().getTranslationKey() + ".destroy_original", " Destroy the original card.");
		add(ModCardAbilities.COPY_CARDS.get().getTranslationKey() + ".restore_health",
				" Restore the copy to full health.");
		add(ModCardAbilities.COPY_CARDS.get().getTranslationKey() + ".you", "you");
		add(ModCardAbilities.COPY_CARDS.get().getTranslationKey() + ".enemy", "the enemy");
		add(ModCardAbilities.SUMMON_CARD.get().getTranslationKey(), "%s summon a %s %s.");
		add(ModCardAbilities.SUMMON_CARD.get().getTranslationKey() + "card_text", "%s (%s/%s)");
		add(ModCardAbilities.MULTI.get().getTranslationKey(), "%s");
		add(ModCardAbilities.CHANCE.get().getTranslationKey(), "%s%% chance: %s");
		add(Helper.gui("card_ability_selection"), "%s%s%s%s");
		add(Helper.gui("where"), " where ");
		add(Helper.gui("if"), " if ");
	}
}
