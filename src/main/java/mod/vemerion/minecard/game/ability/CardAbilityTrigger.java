package mod.vemerion.minecard.game.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import mod.vemerion.minecard.Main;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum CardAbilityTrigger {
	ALWAYS("always"), NEVER("never"), SUMMON("summon");

	public static final Codec<CardAbilityTrigger> CODEC = Codec.STRING.comapFlatMap(s -> {
		for (var e : CardAbilityTrigger.values()) {
			if (e.name.equals(s))
				return DataResult.success(e);
		}
		return DataResult.error("Invalid card ability trigger '" + s + "'");
	}, e -> e.name);

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
