package mod.vemerion.minecard.item;

import java.util.function.Consumer;

import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.renderer.CardItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

public class CardItem extends Item {

	public CardItem() {
		super(new Item.Properties());
	}

	@Override
	public Component getName(ItemStack pStack) {
		return Component.translatable(getDescriptionId(pStack), CardData.getType(pStack)
				.map(rl -> Cards.getInstance(true).get(rl)).map(c -> c.getName()).orElse(Component.empty()));
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return new CardData.Provider(stack);
	}

	@Override
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(new RenderProperties());
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

	private class RenderProperties implements IClientItemExtensions {

		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return new CardItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(),
					Minecraft.getInstance().getEntityModels());
		}
	}
}
