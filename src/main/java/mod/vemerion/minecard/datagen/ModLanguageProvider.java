package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.PlayerStats;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.init.ModBlocks;
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

		advancements();

		add(Helper.gui("stats_general"), "General");
		add(Helper.gui("stats_enemies"), "Opponents");
		add(PlayerStats.Key.textKey(PlayerStats.Key.WINS), "Wins");
		add(PlayerStats.Key.textKey(PlayerStats.Key.LOSSES), "Losses");

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

		// Card abilities
		card("tutorial_creeper", "Attack: Draw 2 cards");
		card("player", "Death: Game Over");
		card("creeper", "Death: Explode and deal 3 damage to adjacent cards.");
		card("donkey", "Summon: Draw a card.");
		card("zombie", "Summon: Gain a random piece of equipment.");
		card("stray", "Attack: Freeze the target.");
		card("enderman", "Summon: Copy a random card from the enemy hand.");
		card("glow_squid", "Summon: Reveal all cards on the board.");
		card("wither_skeleton", "Summon: Reduce the cost of your Wither by two.");
		card("wither", "Tick: Deal 4 damage to a random enemy card.");
		card("squid", "Hurt: Return this card to your hand.");
		card("silverfish", "Hurt: Draw a Silverfish from your deck.");
		card("evoker", "Summon: Deal 2 damage to all enemy cards on the board.");
		card("ender_dragon", "Summon: Create two end crystals.\nTick: Deal 1 damage to all enemy cards on the board.");
		card("rabbit", "Summon: 30% chance to gain +1/+1.");
		card("polar_bear", "Grow: Gain +1/+3 and taunt.");
		card("axolotl", "Hurt: Gain stealth.");
		card("bat", "Attack: Heal your player for one health.");
		card("mule", "Grow: Draw 2 cards.");
		card("pig", "Death: Heal your player for 3 health.");
		card("strider", "Summon: Remove burn from adjacent cards.");
		card("blaze", "Attack: Apply 3 burn to the target.");
		card("ghast", "Summon: Apply 4 burn to the enemy player.");
		card("bamboo", "Grow: Give all pands on the board +2/+2.");
		card("zombified_piglin", "Hurt: Gain 3 attack.");
		card("husk", "Attack: Reduce the attack of your target by 2.");
		card("elder_guardian", "Summon: Reduce the attack of all enemy cards on the board by 1.");
		card("goat", "Summon: Return an enemy card to their hand.");
		card("endermite", "Summon: Draw an Enderman from your deck.");
		card("pillager", "Summon: Give adjacent cards +1/+1 and taunt.");
		card("ravager", "Attack: Also deal damage to the cards adjacent to the target.");
		card("skeleton", "Tick: Deal 1 damage to a random enemy card on the board.");
		card("spider", "Death: Increase the cost of a random card from the enemy hand by 1.");
		card("skeleton_horse", "Summon: Deal 2 damage and apply 2 burn to a random enemy card on the board.");
		card("salmon", "Death: 50% chance to pick up the Salmon in a bucket.");
		card("ocelot", "Summon: Scare all enemy Phantoms and Creepers.");
		card("snow_golem", "Summon: Deal 2 damage and freeze a selected enemy card from the board.");
		card("chicken", "Summon: 50% chance to gain an egg.");
		card("egg",
				"Throw the egg at a selected card on the board, dealing 1 damage and 50% chance to summon a chicken.");
		card("scute", "Give a selected card from the board 5 health and taunt.");
		card("milk_bucket", "Remove burn/freeze/shield/stealth/taunt/thorns/poison from a selected card.");
		card("mushroom_stew", "Select a card from the board and make it grow up instantly.");
		card("llama", "Summon: Deal 1 damage to a selected card on the board.");
		card("cat", "Tick: 50% chance to give you a gift.");
		card("cave_spider", "Attack: 50% chance to poison the target.");
		card("wandering_trader", "Summon: Do a trade!");
		card("trader_llama", "Summon: Either deal 2 damage to a selected card on the board, or draw a card.");
		card("trader_llama_spit", "Deal 2 damage to a selected card on the board.");
		card("trader_llama_draw", "Draw a card.");
		card("zombie_villager", "Summon: Give all your undead cards on the board +2/+2.");
		card("hoglin", "Summon: Either gain +1/+1 and taunt, or summon a 4/3 Hoglin baby.");
		card("hoglin_baby", "Summon a 4/3 Hoglin baby.");
		card("hoglin_buff", "Gain +1/+1 and taunt.");
		card("fishing_rod", "Steal a selected card from the enemy hand.");
		card("book", "Draw 2 cards.");
		card("splash_potion_of_harming", "Deal 3 damage to 2 random enemy cards on the board.");
		card("enchanted_golden_apple", "Give a selected friendly card on the board 6 health, shield, and remove burn.");
		card("chest", "Draw 3 cards, but increase their cost by 1.");
		card("enchanted_book", "Draw a card, and give it +2/+2.");
		card("spyglass", "Discover a card from the enemy hand. Set its cost to 10.");
		card("lodestone", "Swap hand with your enemy.");
		card("soul_sand", "Deal X damage to the enemy board. X = number of enemy cards on the board.");
		card("amethyst_shard",
				"Draw a card. Deal damage equal to the cost of the drawn card to a random enemy card on the board.");
		card("end_crystal", "Tick: Heal adjacent Ender Dragons by 4.");
		card("trident",
				"Deal 4 damage and apply 4 burn to a selected card from the board. 30% chance to return this card to your hand.");
		card("throw_sweet_berries", "Select a card from the board and heal it by 4.");
		card("rotten_flesh", "Select a card from the board and either heal or hurt it, choosen randomly.");
		card("rabbit_foot", "Select a card from the board and give it 1 health.");
		card("absorption_potion", "Select a card from the board and give it shield.");
		card("poison_potion", "Select a card from the board and give it poison.");
		card("healing_potion", "Select a card from the board and heal it by 5.");
		card("packed_ice", "Select a card from the board and freeze it.");
		card("pointed_dripstone", "Select a card from the board and give it 3 thorns.");
		card("iron_sword", "Select a card from the board and give it 2 attack.");
		card("emerald", "Gain 1 temporary resource.");
		card("leather_chestplate", "Select a card from the board and give it 2 health.");
		card("iron_boots", "Select a card from the board and give it 2 health.");
		card("fire_charge", "Select a card from the board and give it 3 burn.");
		card("ender_pearl", "Select a friendly card from the board and return it to your hand.");
		card("cod", "Summon: Chance to summon another Cod.");
		card("splitter", "Death: Split into 2 smaller versions of itself.");
		card("vindicator", "Death: Gain an emerald card.");
		card("sheep", "Death: Summon a wool card for the enemy.");
		card("villager", "Hurt: Summon an Iron Golem.");
		card("bee", "Summon: Spawn a bee nest.");
		card("bee_nest", "Tick: Summon a bee.");
		card("panda", "Summon: Spawn a bamboo card.");
		card("magma_cube", "Attack: Apply 2 burn to the target.");
		card("salmon_bucket", "Summon a Salmon.");
		card("turtle", "Grow: Gain a scute card.");
		card("drowned", "Death: Gain a trident card.");
		card("cow", "Death: Gain a milk bucket card.");
		card("fox", "Summon: Spawn a sweet berries card.");
		card("mooshroom", "Summon: Gain a mushroom stew card.");
		card("witch", "Summon: Gain a random potion card.");
		card("dolphin", "Summon: Spawn a treasure chest.");
		card("zoglin", "Death: Gain a rotten flesh card.");
		card("piglin", "Summon: Gain a random barter card.");
		card("sweet_berries", "Death: Gain a sweet berries card.");
		card("pufferfish_bucket", "Summon: Spawn a Pufferfish card.");
		card("buried_treasure", "Death: Gain a random treasure card.");
		card("wooden_sword", "Give a card on the board 3 attack. The sword is return when the card dies.");
		card("wooden_sword_return", "Death: Return the wooden sword to your hand.");

	}

	public void card(String key, String text) {
		add(ModCardProvider.textKey(key), text);
	}

	private void advancements() {
		add(ModAdvancementProvider.titleKey("root"), "Minecard");
		add(ModAdvancementProvider.descriptionKey("root"), "Collect a card");
		add(ModAdvancementProvider.titleKey("win"), "Victory");
		add(ModAdvancementProvider.descriptionKey("win"), "Win a game");
		add(ModAdvancementProvider.titleKey("tutorial"), "Learned");
		add(ModAdvancementProvider.descriptionKey("tutorial"), "Complete the tutorial");
		add(ModAdvancementProvider.titleKey("win_ai"), "Man vs Machine");
		add(ModAdvancementProvider.descriptionKey("win_ai"), "Win a game against the AI");
		add(ModAdvancementProvider.titleKey("zombie_buff"), "Zombie horde");
		add(ModAdvancementProvider.descriptionKey("zombie_buff"), "Buff 3 other undead cards with a Zombie Villager");
		add(ModAdvancementProvider.titleKey("discount_wither"), "Full discount");
		add(ModAdvancementProvider.descriptionKey("discount_wither"), "Play a Wither card costing 6 or less");
		add(ModAdvancementProvider.titleKey("sweeping_edge"), "Sweeping edge");
		add(ModAdvancementProvider.descriptionKey("sweeping_edge"), "Kill both adjacent cards in one Ravager attack");
		add(ModAdvancementProvider.titleKey("iron_golem_farm"), "Iron golem farm");
		add(ModAdvancementProvider.descriptionKey("iron_golem_farm"), "Summon 3 Iron Golems from a single Villager");
		add(ModAdvancementProvider.titleKey("collect_boss"), "Rare collector");
		add(ModAdvancementProvider.descriptionKey("collect_boss"), "Collect a card from one of the vanilla bosses");
		add(ModAdvancementProvider.titleKey("collect_spells"), "Explorer");
		add(ModAdvancementProvider.descriptionKey("collect_spells"), "Collect all spell cards from treasure chests");
		add(ModAdvancementProvider.titleKey("collect_mobs"), "Collect 'em all");
		add(ModAdvancementProvider.descriptionKey("collect_mobs"), "Collect all vanilla non-boss mob cards");

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
