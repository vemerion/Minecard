package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public class CopyCardsAbility extends CardAbility {

	public static final Codec<CopyCardsAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder.create(instance -> instance
							.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger),
									Codec.BOOL.fieldOf("destroy_original").forGetter(CopyCardsAbility::destroyOriginal),
									Codec.BOOL.fieldOf("restore_health").forGetter(CopyCardsAbility::restoreHealth),
									CardAbilitySelection.CODEC.fieldOf("selection")
											.forGetter(CopyCardsAbility::getSelection))
							.apply(instance, CopyCardsAbility::new)));

	private final boolean destroyOriginal;
	private final boolean restoreHealth;
	private final CardAbilitySelection selection;

	public CopyCardsAbility(CardAbilityTrigger trigger, boolean destroyOriginal, boolean restoreHealth,
			CardAbilitySelection selection) {
		super(trigger);
		this.destroyOriginal = destroyOriginal;
		this.restoreHealth = restoreHealth;
		this.selection = selection;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.COPY_CARDS.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		return new Object[] { trigger.getText(), selection.getText(),
				destroyOriginal
						? new TranslatableComponent(
								ModCardAbilities.COPY_CARDS.get().getTranslationKey() + ".destroy_original")
						: TextComponent.EMPTY,
				restoreHealth
						? new TranslatableComponent(
								ModCardAbilities.COPY_CARDS.get().getTranslationKey() + ".restore_health")
						: TextComponent.EMPTY };
	}

	@Override
	protected void invoke(List<ServerPlayer> receivers, PlayerState state, Card card, @Nullable Card other) {
		var selected = selection.select(state.getGame(), state.getId(), card, other);

		var copies = selected.stream()
				.map(c -> new Card(c.getType(), c.getCost(), c.getHealth(), c.getDamage(), c.getMaxHealth(),
						c.getMaxDamage(), false, new HashMap<>(c.getProperties()), c.getAbility(),
						new HashMap<>(c.getEquipment()), c.getAdditionalData()))
				.collect(Collectors.toCollection(() -> new ArrayList<>()));

		for (var copy : copies)
			copy.setHealth(copy.getMaxHealth());

		state.addCards(receivers, copies);

		if (destroyOriginal)
			for (var c : selected)
				state.removeCard(receivers, c);
	}

	public CardAbilitySelection getSelection() {
		return selection;
	}

	public boolean destroyOriginal() {
		return destroyOriginal;
	}

	public boolean restoreHealth() {
		return restoreHealth;
	}

}
