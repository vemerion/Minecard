package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.world.item.ItemStack;

public class DrawCardsAbility extends CardAbility {

	public static final Codec<DrawCardsAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(GameUtil.TRIGGERS_CODEC.optionalFieldOf("triggers", Set.of()).forGetter(CardAbility::getTriggers),
					Codec.STRING.optionalFieldOf("text_key", "").forGetter(CardAbility::getTextKey),
					Codec.INT.fieldOf("count").forGetter(DrawCardsAbility::getCount))
			.apply(instance, DrawCardsAbility::new));

	private final int count;

	public DrawCardsAbility(Set<CardAbilityTrigger> triggers, String textKey, int count) {
		super(triggers, textKey);
		this.count = count;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.DRAW_CARDS.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected, ItemStack icon) {
		var drawn = state.drawCards(receivers, count);
		collected.get(0).addAll(drawn);

		state.getGame().addHistory(receivers, new HistoryEntry(icon, state.getId(), card, List.of()));
	}

	public int getCount() {
		return count;
	}

}
