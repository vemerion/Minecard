package mod.vemerion.minecard.item;

import mod.vemerion.minecard.capability.DeckData;
import mod.vemerion.minecard.menu.DeckMenu;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.network.NetworkHooks;

public class DeckItem extends Item {

	public DeckItem() {
		super(new Item.Properties().stacksTo(1));
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
		return new DeckData.Provider();
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player playerIn, InteractionHand handIn) {
		var stack = playerIn.getItemInHand(handIn);
		if (!level.isClientSide) {
			DeckData.get(stack).ifPresent(data -> {
				NetworkHooks.openGui((ServerPlayer) playerIn, new SimpleMenuProvider(
						(id, inventory, player) -> new DeckMenu(id, inventory, data), getName(stack)));
			});
		}

		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
	}

}
