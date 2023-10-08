package mod.vemerion.minecard.init;

import javax.annotation.Nullable;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.item.CardItem;
import mod.vemerion.minecard.item.DeckItem;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@EventBusSubscriber(bus = Bus.MOD, modid = Main.MODID)
public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);

	public static final RegistryObject<Item> EMPTY_CARD_FRONT = ITEMS.register("empty_card_front",
			() -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> EMPTY_CARD_BACK = ITEMS.register("empty_card_back",
			() -> new Item(new Item.Properties()));
	public static final RegistryObject<Item> EMPTY_CARD_FULL = ITEMS.register("empty_card_full",
			() -> new Item(new Item.Properties()));
	public static final RegistryObject<CardItem> CARD = ITEMS.register("card", () -> new CardItem());
	public static final RegistryObject<DeckItem> DECK = ITEMS.register("deck", () -> new DeckItem());
	public static final RegistryObject<Item> GAME = ITEMS.register("game",
			() -> new BlockItem(ModBlocks.GAME.get(), new Item.Properties()) {
				@Override
				public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
					return 300;
				}
			});

	@SubscribeEvent
	public static void onRegisterTabs(CreativeModeTabEvent.Register event) {
		event.registerCreativeModeTab(new ResourceLocation(Main.MODID, "tab"),
				builder -> builder.title(Component.translatable("itemGroup." + Main.MODID))
						.icon(() -> DECK.get().getDefaultInstance()).displayItems((params, output) -> {
							output.accept(new ItemStack(DECK.get()));
							output.accept(new ItemStack(GAME.get()));

							for (var type : ForgeRegistries.ENTITY_TYPES) {
								if (!Cards.isAllowed(type, params.enabledFeatures()))
									continue;
								var stack = new ItemStack(CARD.get());
								CardData.get(stack).ifPresent(data -> {
									data.setType(type);
									output.accept(stack);
								});
							}
							for (var spell : Cards.SPELLS) {
								var stack = new ItemStack(CARD.get());
								CardData.get(stack).ifPresent(data -> {
									data.setType(spell);
									output.accept(stack);
								});
							}
						}));
	}
}
