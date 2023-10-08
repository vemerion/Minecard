package mod.vemerion.minecard.lootmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class CardTreasureLootModifier extends LootModifier {

	public static final Codec<SimpleWeightedRandomList<ResourceLocation>> LIST_CODEC = SimpleWeightedRandomList
			.wrappedCodecAllowingEmpty(ResourceLocation.CODEC);

	public static final Codec<CardTreasureLootModifier> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions),
					LIST_CODEC.fieldOf("cards").forGetter(lm -> lm.list))
			.apply(instance, CardTreasureLootModifier::new));

	private SimpleWeightedRandomList<ResourceLocation> list;

	public CardTreasureLootModifier(LootItemCondition[] ailootcondition,
			SimpleWeightedRandomList<ResourceLocation> list) {
		super(ailootcondition);
		this.list = list;
	}

	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		list.getRandomValue(context.getRandom()).ifPresent(rl -> {
			var card = ModItems.CARD.get().getDefaultInstance();
			CardData.get(card).ifPresent(data -> data.setType(rl));
			generatedLoot.add(card);
		});
		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}

}