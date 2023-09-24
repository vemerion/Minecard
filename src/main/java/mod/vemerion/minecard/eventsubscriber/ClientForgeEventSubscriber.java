package mod.vemerion.minecard.eventsubscriber;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.init.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
					? new TranslatableComponent(ModItems.CARD.get().getDescriptionId() + ".tooltip_spell",
							new TextComponent(String.valueOf(card.getCost())).withStyle(ChatFormatting.BLUE))
							.withStyle(ChatFormatting.ITALIC)
					: new TranslatableComponent(ModItems.CARD.get().getDescriptionId() + ".tooltip_stats",
							new TextComponent(String.valueOf(card.getCost())).withStyle(ChatFormatting.BLUE),
							new TextComponent(String.valueOf(card.getDamage())).withStyle(ChatFormatting.YELLOW),
							new TextComponent(String.valueOf(card.getHealth())).withStyle(ChatFormatting.RED));
			tooltip.add(descr);
			if (card.getAbility().getText() != TextComponent.EMPTY) {
				tooltip.add(Screen.hasShiftDown() ? card.getAbility().getText()
						: new TranslatableComponent(ModItems.CARD.get().getDescriptionId() + ".tooltip_more")
								.withStyle(ChatFormatting.ITALIC));
			}
		});
	}
}
