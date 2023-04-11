package mod.vemerion.minecard;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import mod.vemerion.minecard.init.ModBlockEntities;
import mod.vemerion.minecard.init.ModBlocks;
import mod.vemerion.minecard.init.ModCardAbilities;
import mod.vemerion.minecard.init.ModCardConditions;
import mod.vemerion.minecard.init.ModItems;
import mod.vemerion.minecard.init.ModLootModifiers;
import mod.vemerion.minecard.init.ModMenus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Main.MODID)
public class Main {
	public static final String MODID = "minecard";

	public static final Logger LOGGER = LogUtils.getLogger();

	public Main() {
		var bus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLootModifiers.LOOT_MODIFIERS.register(bus);
		ModItems.ITEMS.register(bus);
		ModBlockEntities.BLOCK_ENTITIES.register(bus);
		ModBlocks.BLOCKS.register(bus);
		ModMenus.MENUS.register(bus);
		ModCardAbilities.CARD_ABILITIES.register(bus);
		ModCardConditions.CARD_CONDITIONS.register(bus);
	}
}
