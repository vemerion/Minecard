package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.init.ModLootModifiers;
import mod.vemerion.minecard.lootmodifier.CardLootModifier;
import net.minecraft.data.DataGenerator;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;

public class ModLootModifierProvider extends GlobalLootModifierProvider {

	public ModLootModifierProvider(DataGenerator gen) {
		super(gen, Main.MODID);
	}

	@Override
	protected void start() {
		add(ModLootModifiers.CARD.get().getRegistryName().getPath(), ModLootModifiers.CARD.get(),
				new CardLootModifier(new LootItemCondition[] {
						LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.1f, 0.02f).build(),
						LootItemKilledByPlayerCondition.killedByPlayer().build() }));
	}
}
