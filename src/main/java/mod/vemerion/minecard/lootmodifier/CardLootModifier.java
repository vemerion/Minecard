package mod.vemerion.minecard.lootmodifier;

import java.util.List;

import com.google.gson.JsonObject;

import mod.vemerion.minecard.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

public class CardLootModifier extends LootModifier {

	public CardLootModifier(LootItemCondition[] ailootcondition) {
		super(ailootcondition);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		generatedLoot.add(ModItems.CARD.get().getDefaultInstance());
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<CardLootModifier> {

		@Override
		public CardLootModifier read(ResourceLocation location, JsonObject json, LootItemCondition[] ailootcondition) {
			return new CardLootModifier(ailootcondition);
		}

		@Override
		public JsonObject write(CardLootModifier instance) {
			JsonObject json = makeConditions(instance.conditions);
			return json;
		}

	}
}