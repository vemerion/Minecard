package mod.vemerion.minecard.lootmodifier;

import java.util.List;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

public class CardTreasureLootModifier extends LootModifier {

	public static final Codec<SimpleWeightedRandomList<ResourceLocation>> LIST_CODEC = SimpleWeightedRandomList
			.wrappedCodecAllowingEmpty(ResourceLocation.CODEC);

	private SimpleWeightedRandomList<ResourceLocation> list;

	public CardTreasureLootModifier(LootItemCondition[] ailootcondition,
			SimpleWeightedRandomList<ResourceLocation> list) {
		super(ailootcondition);
		this.list = list;
	}

	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		list.getRandomValue(context.getRandom()).ifPresent(rl -> {
			var card = ModItems.CARD.get().getDefaultInstance();
			CardData.get(card).ifPresent(data -> data.setType(rl));
			generatedLoot.add(card);
		});
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<CardTreasureLootModifier> {

		@Override
		public CardTreasureLootModifier read(ResourceLocation location, JsonObject json,
				LootItemCondition[] ailootcondition) {
			return new CardTreasureLootModifier(ailootcondition,
					LIST_CODEC.parse(JsonOps.INSTANCE, json.get("cards")).getOrThrow(false, s -> {
					}));
		}

		@Override
		public JsonObject write(CardTreasureLootModifier instance) {
			var json = makeConditions(instance.conditions);
			json.add("cards", LIST_CODEC.encodeStart(JsonOps.INSTANCE, instance.list).getOrThrow(false, s -> {
			}));
			return json;
		}

	}
}