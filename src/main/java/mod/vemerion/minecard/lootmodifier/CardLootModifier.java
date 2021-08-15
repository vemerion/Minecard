package mod.vemerion.minecard.lootmodifier;

import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

public class CardLootModifier extends LootModifier {

	private Item card;

	public CardLootModifier(LootItemCondition[] ailootcondition, Item card) {
		super(ailootcondition);
		this.card = card;
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		generatedLoot.add(new ItemStack(card));
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<CardLootModifier> {

		@Override
		public CardLootModifier read(ResourceLocation location, JsonObject json, LootItemCondition[] ailootcondition) {
			Item card = GsonHelper.getAsItem(json, "item");
			return new CardLootModifier(ailootcondition, card);
		}

		@Override
		public JsonObject write(CardLootModifier instance) {
			JsonObject json = makeConditions(instance.conditions);
			json.addProperty("item", instance.card.getRegistryName().toString());
			return json;
		}

	}
}