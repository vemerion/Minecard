package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;

public class PlaceCardsAbility extends CardAbility {

	public static final Codec<PlaceCardsAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(CardPlacement.CODEC.fieldOf("placement").forGetter(PlaceCardsAbility::getPlacement))
			.apply(instance, PlaceCardsAbility::new));

	private final CardPlacement placement;

	public PlaceCardsAbility(CardPlacement placement) {
		super(Set.of(), "");
		this.placement = placement;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.PLACE_CARDS.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected) {
		List<Card> toAdd = new ArrayList<>();
		for (var c : collected.get(0))
			toAdd.add(new Card(c.getType(), c.getCost(), c.getOriginalCost(), c.getHealth(), c.getMaxHealth(),
					c.getOriginalHealth(), c.getDamage(), c.getOriginalDamage(), new HashMap<>(c.getProperties()),
					c.getAbility(), c.getAdditionalData()));
		switch (placement) {
		case ENEMY:
		case LEFT:
		case RIGHT:
			var id = placement == CardPlacement.ENEMY ? state.getGame().getEnemyPlayerState(state.getId()).getId()
					: state.getId();
			var leftId = -1;
			for (var c : toAdd) {
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
				state.getGame().summonCard(receivers, c, id, leftId);
			}
			break;
		case ENEMY_DECK:
			state.getGame().getEnemyPlayerState(state.getId()).shuffleIn(receivers, toAdd);
			break;
		case ENEMY_HAND:
			state.getGame().getEnemyPlayerState(state.getId()).addCards(receivers, toAdd);
			break;
		case YOUR_DECK:
			state.shuffleIn(receivers, toAdd);
			break;
		case YOUR_HAND:
			state.addCards(receivers, toAdd);
			break;
		}
		collected.clear(0);
		collected.get(0).addAll(toAdd);
	}

	public CardPlacement getPlacement() {
		return placement;
	}

}
