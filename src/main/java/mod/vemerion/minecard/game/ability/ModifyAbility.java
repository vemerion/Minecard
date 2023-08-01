package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class ModifyAbility extends CardAbility {

	public static final Codec<ModifyAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder
							.create(instance -> instance
									.group(ExtraCodecs
											.nonEmptyList(Codec
													.list(ExtraCodecs.nonEmptyList(Codec.list(CardModification.CODEC))))
											.fieldOf("modifications").forGetter(ModifyAbility::getModifications))
									.apply(instance, ModifyAbility::new)));

	private final List<List<CardModification>> modifications;

	public ModifyAbility(List<List<CardModification>> modifications) {
		super(Set.of());
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
			Collected collected, ItemStack icon) {
		var copy = new Card(card);
		var modification = modifications.get(state.getGame().getRandom().nextInt(modifications.size()));

		for (var selected : collected.get(0)) {
			for (var m : modification)
				m.getOutput().set(state, selected, receivers,
						m.getOperator().evaluate(state.getGame().getRandom(), selected));
		}

		if (!icon.isEmpty())
			state.getGame().addHistory(receivers, new HistoryEntry(icon, state.getId(), copy,
					collected.get(0).stream().filter(c -> state.getGame().isInBoard(c) || c.isDead()).toList()));

		for (var receiver : receivers) {
			state.getGame().updateCards(receiver, collected.get(0));
		}
	}

	public List<List<CardModification>> getModifications() {
		return modifications;
	}

}
