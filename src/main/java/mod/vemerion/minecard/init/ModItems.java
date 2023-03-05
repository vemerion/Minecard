package mod.vemerion.minecard.init;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.item.CardItem;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Main.MODID);

	public static final RegistryObject<CardItem> CARD = ITEMS.register("card",
			() -> new CardItem(() -> EntityType.CREEPER));
}
