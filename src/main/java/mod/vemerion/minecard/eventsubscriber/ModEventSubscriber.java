package mod.vemerion.minecard.eventsubscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.item.CardItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.registries.IForgeRegistry;

@EventBusSubscriber(modid = Main.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

	private static List<CardItem> cards;

	@SubscribeEvent
	public static void registerItem(RegistryEvent.Register<Item> event) {
		cards = new ArrayList<>();

		IForgeRegistry<Item> reg = event.getRegistry();

		reg.register(addCard("creeper", () -> EntityType.CREEPER));
	}

	private static CardItem addCard(String name, Supplier<EntityType<?>> type) {
		CardItem card = new CardItem(type);
		card.setRegistryName(new ResourceLocation(Main.MODID, name + "_card"));
		cards.add(card);
		return card;
	}

	public static Iterable<CardItem> getCards() {
		return ImmutableList.copyOf(cards);
	}
}
