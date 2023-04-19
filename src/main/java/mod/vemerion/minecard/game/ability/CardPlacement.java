package mod.vemerion.minecard.game.ability;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.GameUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum CardPlacement {
	LEFT("left"), RIGHT("right"), ENEMY("enemy");

	public static final Codec<CardPlacement> CODEC = GameUtil.enumCodec(CardPlacement.class, CardPlacement::getName);

	private String name;

	private CardPlacement(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTextKey() {
		return "card_placement." + Main.MODID + "." + getName();
	}

	public Component getText() {
		return new TranslatableComponent(getTextKey());
	}
}
