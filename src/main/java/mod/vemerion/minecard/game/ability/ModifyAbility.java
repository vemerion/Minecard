package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.CardVisibility;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.LazyCardType;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import mod.vemerion.minecard.network.AnimationMessage;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class ModifyAbility extends CardAbility {

	public static record Modification(int healthChange, LazyCardType modifications) {
		public static final Codec<Modification> CODEC = ExtraCodecs
				.lazyInitializedCodec(
						() -> RecordCodecBuilder.create(instance -> instance
								.group(Codec.INT.fieldOf("health_change").forGetter(Modification::healthChange),
										LazyCardType.CODEC.fieldOf("modifications")
												.forGetter(Modification::modifications))
								.apply(instance, Modification::new)));
	}

	public static final Codec<ModifyAbility> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(
			instance -> instance.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger),
					ResourceLocation.CODEC.optionalFieldOf("animation").forGetter(ModifyAbility::getAnimation),
					CardAbilitySelection.CODEC.fieldOf("selection").forGetter(ModifyAbility::getSelection),
					ExtraCodecs.nonEmptyList(Codec.list(Modification.CODEC)).fieldOf("modifications")
							.forGetter(ModifyAbility::getModifications))
					.apply(instance, ModifyAbility::new)));

	private final Optional<ResourceLocation> animation;
	private final CardAbilitySelection selection;
	private final List<Modification> modifications;

	public ModifyAbility(CardAbilityTrigger trigger, Optional<ResourceLocation> animation,
			CardAbilitySelection selection, List<Modification> modifications) {
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
		for (var m : modifications) {
			var card = m.modifications.get(true);
			var healthChangeText = m.healthChange == 0 ? TextComponent.EMPTY
					: new TranslatableComponent(
							ModCardAbilities.MODIFY.get().getTranslationKey()
									+ (m.healthChange > 0 ? ".element_heal" : ".element_hurt"),
							Math.abs(m.healthChange));
			var damageText = modifierText(card.getDamage());
			var healthText = modifierText(card.getHealth());
			var costText = card.getCost() == 0 ? TextComponent.EMPTY
					: new TranslatableComponent(ModCardAbilities.MODIFY.get().getTranslationKey() + ".element_cost",
							modifierText(card.getCost()));
			elements.append(new TranslatableComponent(ModCardAbilities.MODIFY.get().getTranslationKey() + ".element",
					GameUtil.propertiesToComponent(card.getProperties()), damageText, healthText, costText,
					healthChangeText));
		}
		return new Object[] { GameUtil.emphasize(trigger.getText()),
				modifications.size() == 1 ? TextComponent.EMPTY
						: new TranslatableComponent(ModCardAbilities.MODIFY.get().getTranslationKey() + ".one_of"),
				elements, selection.getText() };
	}

	private TextComponent modifierText(int value) {
		return new TextComponent((value > 0 ? "+" : "") + String.valueOf(value));
	}

	@Override
	public void createChoices(List<Receiver> receivers, PlayerState state, Card card) {
		selection.createChoice(receivers, this, state, card);
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other) {
		var modification = modifications.get(state.getGame().getRandom().nextInt(modifications.size()));
		var cardType = modification.modifications.get(false);

		if (cardType == null) {
			return;
		}

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
			selected.getEquipment().putAll(cardType.getEquipment());
			selected.setHealth(selected.getHealth() + cardType.getHealth());
			selected.setDamage(selected.getDamage() + cardType.getDamage());
			selected.setMaxHealth(selected.getMaxHealth() + cardType.getHealth());
			selected.setCost(selected.getCost() + cardType.getCost());

			// Properties
			for (var entry : cardType.getProperties().entrySet()) {
				if (entry.getKey() == CardProperty.BABY && entry.getValue() == 0
						&& selected.hasProperty(CardProperty.BABY)) {
					selected.getAbility().onGrow(receivers, state, selected);
				}

				selected.putProperty(entry.getKey(), entry.getValue());
			}
			if (cardType.hasProperty(CardProperty.CHARGE))
				selected.setReady(true);

			var healthChange = modification.healthChange;
			if (healthChange < 0) {
				state.getGame().hurt(receivers, selected, -healthChange);
			} else if (healthChange > 0) {
				state.getGame().heal(receivers, selected, healthChange);
			}
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

	public List<Modification> getModifications() {
		return modifications;
	}

	public CardAbilitySelection getSelection() {
		return selection;
	}

}
