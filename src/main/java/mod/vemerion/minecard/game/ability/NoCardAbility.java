package mod.vemerion.minecard.game.ability;

import java.util.List;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.server.level.ServerPlayer;

public class NoCardAbility extends CardAbility {

	public static final NoCardAbility NO_CARD_ABILITY = new NoCardAbility();

	public static final Codec<NoCardAbility> CODEC = Codec.unit(NO_CARD_ABILITY);

	public NoCardAbility() {
		super(CardAbilityTrigger.NEVER);
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.NO_CARD_ABILITY.get();
	}

	@Override
	protected void invoke(List<ServerPlayer> receivers, PlayerState state, Card card) {

	}

}
