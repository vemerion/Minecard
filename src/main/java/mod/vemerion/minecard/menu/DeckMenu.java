package mod.vemerion.minecard.menu;

import mod.vemerion.minecard.capability.DeckData;
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
	public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
		return ItemStack.EMPTY; // TODO
	}
}
