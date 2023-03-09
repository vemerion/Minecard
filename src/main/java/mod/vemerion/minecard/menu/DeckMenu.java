package mod.vemerion.minecard.menu;

import mod.vemerion.minecard.capability.DeckData;
import mod.vemerion.minecard.init.ModItems;
import mod.vemerion.minecard.init.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;

public class DeckMenu extends AbstractContainerMenu {

	public DeckMenu(int id, Inventory inventory) {
		this(id, inventory, new DeckData());
	}

	public DeckMenu(int id, Inventory inventory, DeckData data) {
		super(ModMenus.DECK.get(), id);

		// Deck slots
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlot(new SlotItemHandler(data, x + y * 9, 8 + x * 18, 18 + y * 18));
			}
		}

		// Player inventory
		for (int y = 0; y < 3; y++) {
			for (int x = 0; x < 9; x++) {
				addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 85 + y * 18));
			}
		}

		// Player hotbar
		for (int x = 0; x < 9; x++) {
			addSlot(new Slot(inventory, x, 8 + x * 18, 143));
		}
	}

	@Override
	public boolean stillValid(Player pPlayer) {
		return true;
	}

	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		var slot = slots.get(index);
		var copy = ItemStack.EMPTY;
		if (slot != null && slot.hasItem()) {
			var stack = slot.getItem();

			if (!stack.is(ModItems.CARD.get())) {
				return copy;
			}

			copy = stack.copy();

			if (index < DeckData.CAPACITY) {
				if (!moveItemStackTo(stack, DeckData.CAPACITY, slots.size(), true))
					return ItemStack.EMPTY;
			} else if (!moveItemStackTo(stack, 0, DeckData.CAPACITY, false)) {
				return ItemStack.EMPTY;
			}

			if (stack.isEmpty())
				slot.set(ItemStack.EMPTY);
			else
				slot.setChanged();
			slot.onTake(playerIn, stack);
		}

		return copy;
	}
}
