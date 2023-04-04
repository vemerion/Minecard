package mod.vemerion.minecard.eventsubscriber;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.screen.GameScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventSubscriber {

	@SubscribeEvent
	public static void fov(FOVModifierEvent event) {
		var mc = Minecraft.getInstance();
		if (mc == null)
			return;

		if (mc.screen instanceof GameScreen game) {
			event.setNewfov(event.getFov() * game.getFovModifier());
		}
	}
}