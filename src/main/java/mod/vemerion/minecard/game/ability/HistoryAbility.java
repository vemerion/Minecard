package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class HistoryAbility extends CardAbility {

	public static final Codec<HistoryAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder.create(instance -> instance
							.group(ItemStack.CODEC.fieldOf("icon").forGetter(HistoryAbility::getIcon),
									HistoryEntry.Visibility.CODEC.optionalFieldOf("visibility")
											.forGetter(HistoryAbility::getVisibility))
							.apply(instance, HistoryAbility::new)));

	private final ItemStack icon;
	private final Optional<HistoryEntry.Visibility> visibility;

	public HistoryAbility(ItemStack icon, Optional<HistoryEntry.Visibility> visibility) {
		super(Set.of(), "");
		this.icon = icon;
		this.visibility = visibility;
	}

	public HistoryAbility(Item icon) {
		this(icon.getDefaultInstance(), Optional.empty());
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.HISTORY.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, Card cause, @Nullable Card target,
			Collected collected) {
		state.getGame().addHistory(receivers, new HistoryEntry(icon, state.getId(), card,
				collected.get(0).stream().map(c -> c.toHistory(calcHistoryVisibility(state, c))).toList()));
	}

	HistoryEntry.Visibility calcHistoryVisibility(PlayerState state, Card card) {
		if (visibility.isPresent())
			return visibility.get();

		switch (state.getGame().calcVisibility(state.getId(), card)) {
		case DECK:
			return HistoryEntry.Visibility.NONE;
		case ENEMY_HAND:
			return HistoryEntry.Visibility.ENEMY;
		case UNKNOWN:
			return HistoryEntry.Visibility.ALL;
		case VISIBLE:
			return state.getGame().isInBoard(card) ? HistoryEntry.Visibility.ALL : HistoryEntry.Visibility.YOU;
		}
		return HistoryEntry.Visibility.NONE;
	}

	public ItemStack getIcon() {
		return icon;
	}

	public Optional<HistoryEntry.Visibility> getVisibility() {
		return visibility;
	}

}
