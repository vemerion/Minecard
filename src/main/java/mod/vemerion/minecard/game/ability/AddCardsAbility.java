package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
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
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;

public class AddCardsAbility extends CardAbility {

	public static final Codec<AddCardsAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger), ExtraCodecs
					.nonEmptyList(Codec.list(LazyCardType.CODEC)).fieldOf("cards").forGetter(AddCardsAbility::getCards))
			.apply(instance, AddCardsAbility::new));

	private final List<LazyCardType> toAdd;

	public AddCardsAbility(CardAbilityTrigger trigger, List<LazyCardType> toAdd) {
		super(trigger);
		this.toAdd = toAdd;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.ADD_CARDS.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		var text = TextComponent.EMPTY.copy();
		if (toAdd.size() > 1)
			text.append(new TranslatableComponent(ModCardAbilities.ADD_CARDS.get().getTranslationKey() + ".one_of"));
		for (var card : toAdd)
			text.append(new TranslatableComponent(ModCardAbilities.ADD_CARDS.get().getTranslationKey() + ".element",
					card.get(true).getName()));
		return new Object[] { trigger.getText(), text };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other) {
		var created = toAdd.get(state.getGame().getRandom().nextInt(toAdd.size())).get(false).create();
		List<Card> added = new ArrayList<>();
		added.add(created);
		state.addCards(receivers, added);

		if (!added.isEmpty())
			state.getGame().addHistory(receivers,
					new HistoryEntry(HistoryEntry.Type.ABILITY, state.getId(), card, added));
	}

	public List<LazyCardType> getCards() {
		return toAdd;
	}

}
