package mod.vemerion.minecard.lootmodifier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class CardLootModifier extends LootModifier {

	public static final Codec<CardLootModifier> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(LOOT_CONDITIONS_CODEC.fieldOf("conditions").forGetter(lm -> lm.conditions))
					.apply(instance, CardLootModifier::new));

	public CardLootModifier(LootItemCondition[] ailootcondition) {
		super(ailootcondition);
	}

	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		var type = context.getParam(LootContextParams.THIS_ENTITY).getType();
		if (!Cards.isAllowed(type, context.getLevel().enabledFeatures())
				|| context.getRandom().nextFloat() >= Cards.getInstance(false).get(type).getDropChance())
			return generatedLoot;

		var card = ModItems.CARD.get().getDefaultInstance();
		CardData.get(card).ifPresent(data -> data.setType(type));
		generatedLoot.add(card);
		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return CODEC;
	}
}