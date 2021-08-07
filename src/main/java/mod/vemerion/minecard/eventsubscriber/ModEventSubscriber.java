package mod.vemerion.minecard.eventsubscriber;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.item.CardItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = Main.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

	@SubscribeEvent
	public static void registerItem(RegistryEvent.Register<Item> event) {
		IForgeRegistry<Item> reg = event.getRegistry();
		
		Item card = new CardItem();
		card.setRegistryName(new ResourceLocation(Main.MODID, "card"));
		reg.register(card);
	}
}
