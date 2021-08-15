package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Cards;
import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.eventsubscriber.ModEventSubscriber;
import mod.vemerion.minecard.item.CardItem;
import mod.vemerion.minecard.lootmodifier.CardLootModifier;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;

public class ModLootModifierProvider extends GlobalLootModifierProvider {

	public ModLootModifierProvider(DataGenerator gen) {
		super(gen, Main.MODID);
	}

	@Override
	protected void start() {
		for (CardItem card : ModEventSubscriber.getCards())
			add(card.getRegistryName().getPath(), Cards.CARD_LOOT_MODIFIER,
					new CardLootModifier(new LootItemCondition[] {
							LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.1f, 0.02f).build(),
							LootItemKilledByPlayerCondition.killedByPlayer().build(),
							LootTableIdCondition.builder(
									new ResourceLocation("entities/" + card.getType().getRegistryName().getPath()))
									.build() },
							card));
	}

}
