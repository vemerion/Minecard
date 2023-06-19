package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardVisibility;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import mod.vemerion.minecard.network.AnimationMessage;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class ModifyAbility extends CardAbility {

	public static final Codec<ModifyAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder
							.create(instance -> instance
									.group(CardAbilityTrigger.CODEC.fieldOf("trigger")
											.forGetter(CardAbility::getTrigger),
											ResourceLocation.CODEC
													.optionalFieldOf("animation")
													.forGetter(ModifyAbility::getAnimation),
											CardAbilitySelection.CODEC.fieldOf("selection")
													.forGetter(ModifyAbility::getSelection),
											ExtraCodecs
													.nonEmptyList(Codec.list(ExtraCodecs
															.nonEmptyList(Codec.list(CardModification.CODEC))))
													.fieldOf("modifications")
													.forGetter(ModifyAbility::getModifications))
									.apply(instance, ModifyAbility::new)));

	private final Optional<ResourceLocation> animation;
	private final CardAbilitySelection selection;
	private final List<List<CardModification>> modifications;

	public ModifyAbility(CardAbilityTrigger trigger, Optional<ResourceLocation> animation,
			CardAbilitySelection selection, List<List<CardModification>> modifications) {
		super(trigger);
		this.animation = animation;
		this.selection = selection;
		this.modifications = modifications;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.MODIFY.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		var elements = TextComponent.EMPTY.copy();
		for (var modification : modifications) {
			var modificationText = TextComponent.EMPTY.copy();
			for (int i = 0; i < modification.size(); i++) {
				modificationText.append(modification.get(i).getText()).append(i < modification.size() - 1 ? ", " : "");
			}
			elements.append(new TranslatableComponent(ModCardAbilities.MODIFY.get().getTranslationKey() + ".element",
					modificationText));
		}
		return new Object[] { GameUtil.emphasize(trigger.getText()),
				modifications.size() == 1 ? TextComponent.EMPTY
						: new TranslatableComponent(ModCardAbilities.MODIFY.get().getTranslationKey() + ".one_of"),
				elements, selection.getText() };
	}

	@Override
	public void createChoices(List<Receiver> receivers, PlayerState state, Card card) {
		selection.createChoice(receivers, this, state, card);
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other) {
		var modification = modifications.get(state.getGame().getRandom().nextInt(modifications.size()));

		var selectedCards = selection.select(state.getGame(), this, state.getId(), card, other);

		animation.ifPresent(anim -> {
			for (var receiver : receivers) {
				receiver.receiver(new AnimationMessage(card.getId(), selectedCards.stream().filter(c -> {
					return state.getGame().calcVisibility(receiver.getId(), c) == CardVisibility.VISIBLE
							|| state.getGame().calcVisibility(state.getId(), c) == CardVisibility.ENEMY_HAND;
				}).map(c -> c.getId()).collect(Collectors.toList()), anim));
			}
		});

		for (var selected : selectedCards) {
			for (var m : modification)
				m.getOutput().set(state, selected, receivers,
						m.getOperator().evaluate(state.getGame().getRandom(), selected));
		}

		state.getGame().addHistory(receivers, new HistoryEntry(HistoryEntry.Type.ABILITY, state.getId(), card,
				selectedCards.stream().filter(c -> state.getGame().isInBoard(c) || c.isDead()).toList()));

		for (var receiver : receivers) {
			state.getGame().updateCards(receiver, selectedCards);
		}
	}

	private Optional<ResourceLocation> getAnimation() {
		return animation;
	}

	public List<List<CardModification>> getModifications() {
		return modifications;
	}

	public CardAbilitySelection getSelection() {
		return selection;
	}

}
