package mod.vemerion.minecard.gametest;

import mod.vemerion.minecard.Main;
import net.minecraft.gametest.framework.GameTestRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = Main.MODID, bus = EventBusSubscriber.Bus.MOD)
public class GametestEventSubscriber {

	@SubscribeEvent
	public static void registerTests(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			GameTestRegistry.register(CardLootModifierTest.class);
		});
	}
}
