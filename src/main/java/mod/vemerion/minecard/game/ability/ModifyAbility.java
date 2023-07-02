package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardVisibility;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import mod.vemerion.minecard.network.AnimationMessage;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class ModifyAbility extends CardAbility {

	public static final Codec<ModifyAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder.create(instance -> instance
							.group(ResourceLocation.CODEC.optionalFieldOf("animation")
									.forGetter(ModifyAbility::getAnimation),
									ExtraCodecs
											.nonEmptyList(Codec
													.list(ExtraCodecs.nonEmptyList(Codec.list(CardModification.CODEC))))
											.fieldOf("modifications").forGetter(ModifyAbility::getModifications))
							.apply(instance, ModifyAbility::new)));

	private final Optional<ResourceLocation> animation;
	private final List<List<CardModification>> modifications;

	public ModifyAbility(Optional<ResourceLocation> animation, List<List<CardModification>> modifications) {
		super(Set.of());
		this.animation = animation;
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
		return new Object[] {
				modifications.size() == 1 ? TextComponent.EMPTY
						: new TranslatableComponent(ModCardAbilities.MODIFY.get().getTranslationKey() + ".one_of"),
				elements };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			List<Card> collected, ItemStack icon) {
		var copy = new Card(card);
		var modification = modifications.get(state.getGame().getRandom().nextInt(modifications.size()));

		animation.ifPresent(anim -> {
			for (var receiver : receivers) {
				receiver.receiver(new AnimationMessage(card.getId(), collected.stream().filter(c -> {
					return state.getGame().calcVisibility(receiver.getId(), c) == CardVisibility.VISIBLE
							|| state.getGame().calcVisibility(state.getId(), c) == CardVisibility.ENEMY_HAND;
				}).map(c -> c.getId()).collect(Collectors.toList()), anim));
			}
		});

		for (var selected : collected) {
			for (var m : modification)
				m.getOutput().set(state, selected, receivers,
						m.getOperator().evaluate(state.getGame().getRandom(), selected));
		}

		if (!icon.isEmpty())
			state.getGame().addHistory(receivers, new HistoryEntry(icon, state.getId(), copy,
					collected.stream().filter(c -> state.getGame().isInBoard(c) || c.isDead()).toList()));

		for (var receiver : receivers) {
			state.getGame().updateCards(receiver, collected);
		}
	}

	private Optional<ResourceLocation> getAnimation() {
		return animation;
	}

	public List<List<CardModification>> getModifications() {
		return modifications;
	}

}
