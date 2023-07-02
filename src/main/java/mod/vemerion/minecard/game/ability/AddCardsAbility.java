package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.LazyCardType;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class AddCardsAbility extends CardAbility {

	public static final Codec<AddCardsAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(GameUtil.TRIGGERS_CODEC.optionalFieldOf("triggers", Set.of()).forGetter(CardAbility::getTriggers),
					ExtraCodecs.nonEmptyList(Codec.list(LazyCardType.CODEC)).fieldOf("cards")
							.forGetter(AddCardsAbility::getCards))
			.apply(instance, AddCardsAbility::new));

	private final List<LazyCardType> toAdd;

	public AddCardsAbility(Set<CardAbilityTrigger> triggers, List<LazyCardType> toAdd) {
		super(triggers);
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
		return new Object[] { GameUtil.emphasize(GameUtil.triggersToText(triggers)), text };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			List<Card> collected, ItemStack icon) {
		var created = toAdd.get(state.getGame().getRandom().nextInt(toAdd.size())).get(false).create();
		List<Card> added = new ArrayList<>();
		added.add(created);
		state.addCards(receivers, added);
		collected.addAll(added);

		if (!added.isEmpty())
			state.getGame().addHistory(receivers, new HistoryEntry(icon, state.getId(), card, added));
	}

	public List<LazyCardType> getCards() {
		return toAdd;
	}

}
