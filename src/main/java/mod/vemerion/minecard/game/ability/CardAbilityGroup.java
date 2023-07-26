package mod.vemerion.minecard.game.ability;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.GameUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum CardAbilityGroup {
	ALL("all"), SELF("self"), TARGET("target"), ENEMY_BOARD("enemy_board"), YOUR_BOARD("your_board"),
	ENEMY_HAND("enemy_hand"), YOUR_HAND("your_hand"), ADJACENT("adjacent"), TARGET_ADJACENT("target_adjacent"),
	ENEMY_DECK("enemy_deck"), YOUR_DECK("your_deck"), COLLECTED("collected");

	public static final Codec<CardAbilityGroup> CODEC = GameUtil.enumCodec(CardAbilityGroup.class,
			CardAbilityGroup::getName);

	private String name;

	private CardAbilityGroup(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getTextKey() {
		return "card_ability_group." + Main.MODID + "." + getName();
	}

	public Component getText() {
		return new TranslatableComponent(getTextKey());
	}

	public boolean singular() {
		return this == SELF || this == TARGET;
	}
}
