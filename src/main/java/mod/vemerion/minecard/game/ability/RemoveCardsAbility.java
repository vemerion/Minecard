package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.world.item.ItemStack;

public class RemoveCardsAbility extends CardAbility {

	public static final Codec<RemoveCardsAbility> CODEC = Codec.unit(RemoveCardsAbility::new);

	public RemoveCardsAbility() {
		super(Set.of());
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.REMOVE_CARDS.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected, ItemStack icon) {
		List<Card> copies = new ArrayList<>();
		for (var c : collected.get(0)) {
			copies.add(new Card(c.getType(), c.getCost(), c.getOriginalCost(), c.getHealth(), c.getMaxHealth(),
					c.getOriginalHealth(), c.getDamage(), c.getOriginalDamage(), new HashMap<>(c.getProperties()),
					c.getAbility(), c.getAdditionalData()));
			state.getGame().removeCard(receivers, c);
		}
		collected.get(0).clear();
		collected.get(0).addAll(copies);
	}

}
