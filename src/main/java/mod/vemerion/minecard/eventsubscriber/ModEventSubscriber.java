package mod.vemerion.minecard.eventsubscriber;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.capability.DeckData;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.OpenGameMessage;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@EventBusSubscriber(modid = Main.MODID, bus = Bus.MOD)
public class ModEventSubscriber {

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(CardData.class);
		event.register(DeckData.class);
	}

	@SubscribeEvent
	public static void setup(FMLCommonSetupEvent event) {
		Network.INSTANCE.registerMessage(0, OpenGameMessage.class, OpenGameMessage::encode, OpenGameMessage::decode,
				OpenGameMessage::handle);
	}
}