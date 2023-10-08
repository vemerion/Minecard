package mod.vemerion.minecard.eventsubscriber;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.init.ModEntities;
import mod.vemerion.minecard.model.CardGameRobotModel;
import mod.vemerion.minecard.renderer.CardGameRobotRenderer;
import mod.vemerion.minecard.screen.DeckScreen;
import mod.vemerion.minecard.screen.animation.config.AnimationConfigs;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = Main.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventSubscriber {

	@SubscribeEvent
	public static void onClientSetupEvent(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			MenuScreens.register(mod.vemerion.minecard.init.ModMenus.DECK.get(), DeckScreen::new);
		});
	}

	@SubscribeEvent
	public static void registerClientReloadListeners(RegisterClientReloadListenersEvent event) {
		event.registerReloadListener(AnimationConfigs.getInstance());
	}

	@SubscribeEvent
	public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(ModEntities.CARD_GAME_ROBOT.get(), CardGameRobotRenderer::new);
	}

	@SubscribeEvent
	public static void onRegisterEntityRendererLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(CardGameRobotModel.LAYER, CardGameRobotModel::createBodyLayer);
	}
}
