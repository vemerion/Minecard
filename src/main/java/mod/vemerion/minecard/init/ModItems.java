package mod.vemerion.minecard.init;

import javax.annotation.Nullable;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.item.CardItem;
import mod.vemerion.minecard.item.DeckItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);

	public static final RegistryObject<Item> EMPTY_CARD_FRONT = ITEMS.register("empty_card_front", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> EMPTY_CARD_BACK = ITEMS.register("empty_card_back", () -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> EMPTY_CARD_FULL = ITEMS
			.register("empty_card_full", () -> new Item(new Item.Properties()));
	public static final RegistryObject<CardItem> CARD = ITEMS.register("card", () -> new CardItem());
	public static final RegistryObject<DeckItem> DECK = ITEMS.register("deck", () -> new DeckItem());
	public static final RegistryObject<Item> GAME = ITEMS.register("game",
			() -> new BlockItem(ModBlocks.GAME.get(), new Item.Properties().tab(CreativeModeTab.TAB_SEARCH)) {
				@Override
				public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
					return 300;
				}
			});

	public static final ModCreativeModeTab MOD_CREATIVE_MODE_TAB = new ModCreativeModeTab();

	private static class ModCreativeModeTab extends CreativeModeTab {

		public ModCreativeModeTab() {
			super(Main.MODID);
		}

		@Override
		public ItemStack makeIcon() {
			return new ItemStack(DECK.get());
		}

		@Override
		public void fillItemList(NonNullList<ItemStack> items) {
			items.add(new ItemStack(DECK.get()));
			items.add(new ItemStack(GAME.get()));
			CARD.get().fillItemCategory(MOD_CREATIVE_MODE_TAB, items);
		}
	}
}
