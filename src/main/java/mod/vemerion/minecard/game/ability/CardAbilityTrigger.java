package mod.vemerion.minecard.game.ability;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.GameUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum CardAbilityTrigger {
	ALWAYS("always"), NEVER("never"), SUMMON("summon"), ATTACK("attack"), DEATH("death"), HURT("hurt"), TICK("tick"), GROW("grow");

	public static final Codec<CardAbilityTrigger> CODEC = GameUtil.enumCodec(CardAbilityTrigger.class,
			CardAbilityTrigger::getName);

	private String name;

	private CardAbilityTrigger(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTextKey() {
		return "card_ability_trigger." + Main.MODID + "." + getName();
	}

	public Component getText() {
		return new TranslatableComponent(getTextKey());
	}
}
