package mod.vemerion.minecard.item;

import java.util.function.Consumer;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.init.ModItems;
import mod.vemerion.minecard.renderer.CardItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;

public class CardItem extends Item {

	public CardItem() {
		super(new Item.Properties().tab(ModItems.MOD_CREATIVE_MODE_TAB));
	}

	@Override
	public Component getName(ItemStack pStack) {
		return new TranslatableComponent(getDescriptionId(pStack), CardData.getType(pStack)
				.map(rl -> Cards.getInstance(true).get(rl)).map(c -> c.getName()).orElse(TextComponent.EMPTY));
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return new CardData.Provider(stack);
	}

	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(new RenderProperties());
	}

	@Override
	public void fillItemCategory(CreativeModeTab pGroup, NonNullList<ItemStack> pItems) {
		if (allowdedIn(pGroup)) {
			for (var type : ForgeRegistries.ENTITIES) {
				if (!Cards.isAllowed(type))
					continue;
				var stack = new ItemStack(this);
				CardData.get(stack).ifPresent(data -> {
					data.setType(type);
					pItems.add(stack);
				});
			}
			addSpells(pItems, "fishing_rod", "book", "splash_potion_of_harming", "enchanted_golden_apple", "chest",
					"enchanted_book", "spyglass", "lodestone", "firework_rocket", "amethyst_shard", "wooden_sword");
		}

	}

	void addSpells(NonNullList<ItemStack> items, String... spells) {
		for (var spell : spells) {
			var stack = new ItemStack(this);
			CardData.get(stack).ifPresent(data -> {
				data.setType(new ResourceLocation(Main.MODID, spell));
				items.add(stack);
			});
		}
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
		var stack = player.getItemInHand(hand);

		player.startUsingItem(hand);
		return InteractionResultHolder.pass(stack);
	}

	@Override
	public int getUseDuration(ItemStack stack) {
		return 72000;
	}

	@Override
	public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int count) {
		if (level.isClientSide && count % 10 == 0) {
			CardData.getType(stack).ifPresent(t -> {
				var card = Cards.getInstance(true).get(t).getCardForRendering();
				card.setTextScroll(card.getTextScroll() + 1);
			});
		}
	}

	private class RenderProperties implements IItemRenderProperties {

		@Override
		public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
			return new CardItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}
}
