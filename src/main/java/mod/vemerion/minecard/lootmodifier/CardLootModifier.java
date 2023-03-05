package mod.vemerion.minecard.lootmodifier;

import java.util.List;

import com.google.gson.JsonObject;

import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

public class CardLootModifier extends LootModifier {

	public CardLootModifier(LootItemCondition[] ailootcondition) {
		super(ailootcondition);
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		var card = ModItems.CARD.get().getDefaultInstance();
		CardData.get(card).ifPresent(data -> data.setType(context.getParam(LootContextParams.THIS_ENTITY).getType()));
		generatedLoot.add(card);
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