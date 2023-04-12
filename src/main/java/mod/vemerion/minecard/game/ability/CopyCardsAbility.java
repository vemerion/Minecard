package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public class CopyCardsAbility extends CardAbility {

	public static final Codec<CopyCardsAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder.create(instance -> instance
							.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger),
									CardAbilitySelection.CODEC.fieldOf("selection")
											.forGetter(CopyCardsAbility::getSelection))
							.apply(instance, CopyCardsAbility::new)));

	private final CardAbilitySelection selection;

	public CopyCardsAbility(CardAbilityTrigger trigger, CardAbilitySelection selection) {
		super(trigger);
		this.selection = selection;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.COPY_CARDS.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		return new Object[] { trigger.getText(), selection.getText() };
	}

	@Override
	protected void invoke(List<ServerPlayer> receivers, PlayerState state, Card card, @Nullable Card other) {
		var copies = selection.select(state.getGame(), state.getId(), card, other).stream()
				.map(c -> new Card(c.getType(), c.getCost(), c.getHealth(), c.getDamage(), c.getMaxHealth(),
						c.getMaxDamage(), false, new HashMap<>(c.getProperties()), c.getAbility(),
						new HashMap<>(c.getEquipment()), c.getAdditionalData()))
				.collect(Collectors.toCollection(() -> new ArrayList<>()));

		state.addCards(receivers, copies);
	}

	public CardAbilitySelection getSelection() {
		return selection;
	}

}
