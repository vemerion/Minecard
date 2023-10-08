package mod.vemerion.minecard.eventsubscriber;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(modid = Main.MODID, bus = EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEventSubscriber {

	@SubscribeEvent
	public static void cardTooltip(ItemTooltipEvent event) {
		CardData.getType(event.getItemStack()).ifPresent(t -> {
			var tooltip = event.getToolTip();
			var card = Cards.getInstance(true).get(t);
			var descr = card.isSpell()
					? Component
							.translatable(ModItems.CARD.get().getDescriptionId() + ".tooltip_spell",
									Component.literal(String.valueOf(card.getCost())).withStyle(ChatFormatting.BLUE))
							.withStyle(ChatFormatting.ITALIC)
					: Component.translatable(ModItems.CARD.get().getDescriptionId() + ".tooltip_stats",
							Component.literal(String.valueOf(card.getCost())).withStyle(ChatFormatting.BLUE),
							Component.literal(String.valueOf(card.getDamage())).withStyle(ChatFormatting.YELLOW),
							Component.literal(String.valueOf(card.getHealth())).withStyle(ChatFormatting.RED));
			tooltip.add(descr);
			if (card.getAbility().getText() != CommonComponents.EMPTY) {
				tooltip.add(Screen.hasShiftDown() ? card.getAbility().getText()
						: Component.translatable(ModItems.CARD.get().getDescriptionId() + ".tooltip_more")
								.withStyle(ChatFormatting.ITALIC));
			}
		});
	}
}
