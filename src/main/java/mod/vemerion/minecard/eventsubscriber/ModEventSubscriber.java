package mod.vemerion.minecard.eventsubscriber;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.capability.DeckData;
import mod.vemerion.minecard.network.AttackMessage;
import mod.vemerion.minecard.network.CombatMessage;
import mod.vemerion.minecard.network.DrawCardMessage;
import mod.vemerion.minecard.network.EndTurnMessage;
import mod.vemerion.minecard.network.GameOverMessage;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.NewTurnMessage;
import mod.vemerion.minecard.network.OpenGameMessage;
import mod.vemerion.minecard.network.PlaceCardMessage;
import mod.vemerion.minecard.network.PlayCardMessage;
import mod.vemerion.minecard.network.SetReadyMessage;
import mod.vemerion.minecard.network.SetResourcesMessage;
import mod.vemerion.minecard.network.UpdateCardMessage;
import mod.vemerion.minecard.network.UpdateCardTypesMessage;
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
		Network.INSTANCE.registerMessage(1, NewTurnMessage.class, NewTurnMessage::encode, NewTurnMessage::decode,
				NewTurnMessage::handle);
		Network.INSTANCE.registerMessage(2, EndTurnMessage.class, EndTurnMessage::encode, EndTurnMessage::decode,
				EndTurnMessage::handle);
		Network.INSTANCE.registerMessage(3, SetResourcesMessage.class, SetResourcesMessage::encode,
				SetResourcesMessage::decode, SetResourcesMessage::handle);
		Network.INSTANCE.registerMessage(4, PlayCardMessage.class, PlayCardMessage::encode, PlayCardMessage::decode,
				PlayCardMessage::handle);
		Network.INSTANCE.registerMessage(5, PlaceCardMessage.class, PlaceCardMessage::encode, PlaceCardMessage::decode,
				PlaceCardMessage::handle);
		Network.INSTANCE.registerMessage(6, SetReadyMessage.class, SetReadyMessage::encode, SetReadyMessage::decode,
				SetReadyMessage::handle);
		Network.INSTANCE.registerMessage(7, AttackMessage.class, AttackMessage::encode, AttackMessage::decode,
				AttackMessage::handle);
		Network.INSTANCE.registerMessage(8, UpdateCardMessage.class, UpdateCardMessage::encode,
				UpdateCardMessage::decode, UpdateCardMessage::handle);
		Network.INSTANCE.registerMessage(9, DrawCardMessage.class, DrawCardMessage::encode, DrawCardMessage::decode,
				DrawCardMessage::handle);
		Network.INSTANCE.registerMessage(10, GameOverMessage.class, GameOverMessage::encode, GameOverMessage::decode,
				GameOverMessage::handle);
		Network.INSTANCE.registerMessage(11, CombatMessage.class, CombatMessage::encode, CombatMessage::decode,
				CombatMessage::handle);
		Network.INSTANCE.registerMessage(12, UpdateCardTypesMessage.class, UpdateCardTypesMessage::encode,
				UpdateCardTypesMessage::decode, UpdateCardTypesMessage::handle);
	}
}