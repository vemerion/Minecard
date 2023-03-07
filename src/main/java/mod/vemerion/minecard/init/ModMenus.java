package mod.vemerion.minecard.init;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.menu.DeckMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenus {
	public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.CONTAINERS,
			Main.MODID);

	public static final RegistryObject<MenuType<DeckMenu>> DECK = MENUS.register("deck",
			() -> new MenuType<>(DeckMenu::new));
}
