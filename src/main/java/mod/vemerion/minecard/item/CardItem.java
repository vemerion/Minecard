package mod.vemerion.minecard.item;

import java.util.function.Consumer;
import java.util.function.Supplier;

import mod.vemerion.minecard.renderer.CardItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.IItemRenderProperties;

public class CardItem extends Item {

	private final Supplier<EntityType<?>> TYPE;
	private Entity entity;

	public CardItem(Supplier<EntityType<?>> type) {
		super(new Item.Properties());
		this.TYPE = type;
	}

	public EntityType<?> getType() {
		return TYPE.get();
	}

	public Entity getEntity(Level level) {
		if (entity == null)
			entity = getType().create(level);
		return entity;
	}

	public Component getCardText() {
		return new TranslatableComponent(getCardTextId());
	}

	public String getCardTextId() {
		return "item." + getRegistryName().getNamespace() + "." + getRegistryName().getPath() + ".description";
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
