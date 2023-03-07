package mod.vemerion.minecard.capability;

import mod.vemerion.minecard.init.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public class DeckData extends ItemStackHandler {

	public static final Capability<DeckData> CAPABILITY = CapabilityManager.get(new CapabilityToken<DeckData>() {
	});

	public static final int CAPACITY = 3 * 9;

	public DeckData() {
		super(CAPACITY);
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return stack.is(ModItems.CARD.get());
	}

	public static LazyOptional<DeckData> get(ItemStack stack) {
		return stack.getCapability(CAPABILITY);
	}

	public static class Provider implements ICapabilitySerializable<CompoundTag> {

		public Provider() {
		}

		private LazyOptional<DeckData> instance = LazyOptional.of(() -> new DeckData());

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return CAPABILITY.orEmpty(cap, instance);
		}

		@Override
		public CompoundTag serializeNBT() {
			return instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!"))
					.serializeNBT();
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!"))
					.deserializeNBT(nbt);
		}
	}
}
