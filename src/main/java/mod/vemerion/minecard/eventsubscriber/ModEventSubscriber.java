package mod.vemerion.minecard.eventsubscriber;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.capability.DeckData;
import mod.vemerion.minecard.network.AnimationMessage;
import mod.vemerion.minecard.network.AttackMessage;
import mod.vemerion.minecard.network.CloseGameMessage;
import mod.vemerion.minecard.network.CombatMessage;
import mod.vemerion.minecard.network.DrawCardsMessage;
import mod.vemerion.minecard.network.EndTurnMessage;
import mod.vemerion.minecard.network.GameOverMessage;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.NewTurnMessage;
import mod.vemerion.minecard.network.OpenGameMessage;
import mod.vemerion.minecard.network.PlaceCardMessage;
import mod.vemerion.minecard.network.PlayCardMessage;
import mod.vemerion.minecard.network.SetPropertiesMessage;
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
		int id = 0;

		Network.INSTANCE.registerMessage(id++, OpenGameMessage.class, OpenGameMessage::encode, OpenGameMessage::decode,
				OpenGameMessage::handle);
		Network.INSTANCE.registerMessage(id++, NewTurnMessage.class, NewTurnMessage::encode, NewTurnMessage::decode,
				NewTurnMessage::handle);
		Network.INSTANCE.registerMessage(id++, EndTurnMessage.class, EndTurnMessage::encode, EndTurnMessage::decode,
				EndTurnMessage::handle);
		Network.INSTANCE.registerMessage(id++, SetResourcesMessage.class, SetResourcesMessage::encode,
				SetResourcesMessage::decode, SetResourcesMessage::handle);
		Network.INSTANCE.registerMessage(id++, PlayCardMessage.class, PlayCardMessage::encode, PlayCardMessage::decode,
				PlayCardMessage::handle);
		Network.INSTANCE.registerMessage(id++, PlaceCardMessage.class, PlaceCardMessage::encode,
				PlaceCardMessage::decode, PlaceCardMessage::handle);
		Network.INSTANCE.registerMessage(id++, SetReadyMessage.class, SetReadyMessage::encode, SetReadyMessage::decode,
				SetReadyMessage::handle);
		Network.INSTANCE.registerMessage(id++, AttackMessage.class, AttackMessage::encode, AttackMessage::decode,
				AttackMessage::handle);
		Network.INSTANCE.registerMessage(id++, DrawCardsMessage.class, DrawCardsMessage::encode,
				DrawCardsMessage::decode, DrawCardsMessage::handle);
		Network.INSTANCE.registerMessage(id++, GameOverMessage.class, GameOverMessage::encode, GameOverMessage::decode,
				GameOverMessage::handle);
		Network.INSTANCE.registerMessage(id++, CombatMessage.class, CombatMessage::encode, CombatMessage::decode,
				CombatMessage::handle);
		Network.INSTANCE.registerMessage(id++, UpdateCardTypesMessage.class, UpdateCardTypesMessage::encode,
				UpdateCardTypesMessage::decode, UpdateCardTypesMessage::handle);
		Network.INSTANCE.registerMessage(id++, SetPropertiesMessage.class, SetPropertiesMessage::encode,
				SetPropertiesMessage::decode, SetPropertiesMessage::handle);
		Network.INSTANCE.registerMessage(id++, CloseGameMessage.class, CloseGameMessage::encode,
				CloseGameMessage::decode, CloseGameMessage::handle);
		Network.INSTANCE.registerMessage(id++, UpdateCardMessage.class, UpdateCardMessage::encode,
				UpdateCardMessage::decode, UpdateCardMessage::handle);
		Network.INSTANCE.registerMessage(id++, AnimationMessage.class, AnimationMessage::encode,
				AnimationMessage::decode, AnimationMessage::handle);

	}
}