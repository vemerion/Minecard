package mod.vemerion.minecard.game.ability;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.LazyCardType;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;

public class SummonCardAbility extends CardAbility {

	public static final Codec<SummonCardAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger),
							CardPlacement.CODEC.fieldOf("placement").forGetter(SummonCardAbility::getPlacement),
							LazyCardType.CODEC.fieldOf("card").forGetter(SummonCardAbility::getSummon))
					.apply(instance, SummonCardAbility::new)));

	private final CardPlacement placement;
	private final LazyCardType summon;

	public SummonCardAbility(CardAbilityTrigger trigger, CardPlacement placement, LazyCardType summon) {
		super(trigger);
		this.placement = placement;
		this.summon = summon;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.SUMMON_CARD.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		var card = summon.get(true);
		var cardText = new TranslatableComponent(ModCardAbilities.SUMMON_CARD.get().getTranslationKey() + "card_text",
				card.getName(), card.getDamage(), card.getHealth());
		return new Object[] { trigger.getText(), cardText, placement.getText() };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other) {

		var id = placement == CardPlacement.ENEMY ? state.getGame().getEnemyPlayerState(state.getId()).getId()
				: state.getId();
		var leftId = -1;

		if (placement != CardPlacement.ENEMY) {
			var board = state.getBoard();
			for (int i = 0; i < board.size(); i++) {
				if (board.get(i).getId() == card.getId()) {
					if (placement == CardPlacement.LEFT && i != 0) {
						leftId = board.get(i - 1).getId();
					} else if (placement == CardPlacement.RIGHT) {
						leftId = board.get(i).getId();
					}
				}
			}
		}

		var summoned = summon.get(false).create();

		state.getGame().addHistory(receivers,
				new HistoryEntry(HistoryEntry.Type.ABILITY, state.getId(), card, List.of(summoned)));

		state.getGame().summonCard(receivers, summoned, id, leftId);
	}

	public LazyCardType getSummon() {
		return summon;
	}

	public CardPlacement getPlacement() {
		return placement;
	}

}
