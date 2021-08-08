package mod.vemerion.minecard.item;

import java.util.function.Consumer;

import mod.vemerion.minecard.renderer.CardItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.IItemRenderProperties;

public class CardItem extends Item {

	public CardItem() {
		super(new Item.Properties());
	}

	@Override
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(new RenderProperties());
	}

	private class RenderProperties implements IItemRenderProperties {

		@Override
		public BlockEntityWithoutLevelRenderer getItemStackRenderer() {
			return new CardItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}
}
