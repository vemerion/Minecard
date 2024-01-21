package mod.vemerion.minecard.game.ability;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.game.GameUtil;

public enum CardAbilityTrigger {
	SUMMON("summon"), ATTACK("attack"), DEATH("death"), HURT("hurt"), TICK("tick"), GROW("grow"),
	OTHER_ATTACK_POST("other_attack_post");

	public static final Codec<CardAbilityTrigger> CODEC = GameUtil.enumCodec(CardAbilityTrigger.class,
			CardAbilityTrigger::getName);

	private String name;

	private CardAbilityTrigger(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
