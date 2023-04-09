package mod.vemerion.minecard.item;

import java.util.function.Consumer;

import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.init.ModItems;
import mod.vemerion.minecard.renderer.CardItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.registries.ForgeRegistries;

public class CardItem extends Item {

	public CardItem() {
		super(new Item.Properties().tab(ModItems.MOD_CREATIVE_MODE_TAB));
	}

	public EntityType<?> getType(ItemStack stack) {
		return CardData.getType(stack).orElse(EntityType.PIG);
	}

	@Override
	public Component getName(ItemStack pStack) {
		return new TranslatableComponent(getDescriptionId(pStack), getType(pStack).getDescription());
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
