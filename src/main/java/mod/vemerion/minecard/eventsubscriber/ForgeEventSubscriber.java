package mod.vemerion.minecard.eventsubscriber;

import java.util.List;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.capability.StatsData;
import mod.vemerion.minecard.game.CardProperties;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.init.ModAdvancements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
public class ForgeEventSubscriber {

	@SubscribeEvent
	public static void addReloadListeners(AddReloadListenerEvent event) {
		event.addListener(Cards.getInstance(false));
		event.addListener(CardProperties.getInstance(false));
	}

	@SubscribeEvent
	public static void synchDataDriven(PlayerLoggedInEvent event) {
		Cards.getInstance(false).sendAllCardTypeMessage((ServerPlayer) event.getPlayer());
		CardProperties.getInstance(false).sendAllCardPropertiesMessage((ServerPlayer) event.getPlayer());
	}

	@SubscribeEvent
	public static void collectCard(PlayerTickEvent event) {
		if (event.player instanceof ServerPlayer player) {
			var inv = player.getInventory();
			for (var l : List.of(inv.items, inv.offhand)) {
				for (var stack : l) {
					CardData.getType(stack).ifPresent(rl -> {
						ModAdvancements.COLLECT_CARD.trigger(player, rl);
					});
				}
			}
		}
	}

	private static final ResourceLocation PLAYER_STATS_ID = new ResourceLocation(Main.MODID, "playerstats");

	@SubscribeEvent
	public static void attachCapability(AttachCapabilitiesEvent<Level> event) {
		event.addCapability(PLAYER_STATS_ID, new StatsData.Provider());
	}
}