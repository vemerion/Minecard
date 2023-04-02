package mod.vemerion.minecard.eventsubscriber;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.Cards;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
public class ForgeEventSubscriber {

	@SubscribeEvent
	public static void addCardsReloadListener(AddReloadListenerEvent event) {
		event.addListener(Cards.getInstance(false));
	}

	@SubscribeEvent
	public static void synchMagics(PlayerLoggedInEvent event) {
		Cards.getInstance(false).sendAllCardTypeMessage((ServerPlayer) event.getPlayer());
	}
}