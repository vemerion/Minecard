package mod.vemerion.minecard.capability;

import java.util.Optional;

import mod.vemerion.minecard.Main;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

public class CardData implements INBTSerializable<CompoundTag> {

	public static final Capability<CardData> CAPABILITY = CapabilityManager.get(new CapabilityToken<CardData>() {
	});

	private static final String NBT_KEY = Main.MODID + "_carddata";

	private ItemStack owner;
	private ResourceLocation type;

	public CardData(ItemStack owner) {
		this.owner = owner;

		var nbt = owner.getOrCreateTag();
		if (nbt.contains(NBT_KEY))
			deserializeNBT(nbt.getCompound(NBT_KEY));
	}

	public void setType(ResourceLocation type) {
		this.type = type;
		owner.getOrCreateTag().put(NBT_KEY, serializeNBT());
	}

	public void setType(EntityType<?> type) {
		setType(type.getRegistryName());
	}

	public static LazyOptional<CardData> get(ItemStack stack) {
		return stack.getCapability(CAPABILITY);
	}

	public static Optional<ResourceLocation> getType(ItemStack stack) {
		return get(stack).map(data -> data.type);
	}

	@Override
	public CompoundTag serializeNBT() {
		var tag = new CompoundTag();
		tag.putString("type", type.toString());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		setType(new ResourceLocation(nbt.getString("type")));
	}

	public static class Provider implements ICapabilitySerializable<CompoundTag> {

		public Provider(ItemStack owner) {
			this.owner = owner;
		}

		private ItemStack owner;
		private LazyOptional<CardData> instance = LazyOptional.of(() -> new CardData(owner));

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
