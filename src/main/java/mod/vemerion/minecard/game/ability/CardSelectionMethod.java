package mod.vemerion.minecard.game.ability;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.GameUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum CardSelectionMethod {
	ALL("all"), RANDOM("random"), CHOICE("choice");

	public static final Codec<CardSelectionMethod> CODEC = GameUtil.enumCodec(CardSelectionMethod.class,
			CardSelectionMethod::getName);

	private String name;

	private CardSelectionMethod(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTextKey() {
		return "card_selection_method." + Main.MODID + "." + getName();
	}

	public Component getText() {
		return new TranslatableComponent(getTextKey());
	}
}
