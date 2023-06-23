package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
import net.minecraft.world.item.ItemStack;

public class CopyCardsAbility extends CardAbility {

	public static final Codec<CopyCardsAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(GameUtil.TRIGGERS_CODEC.fieldOf("triggers").forGetter(CardAbility::getTriggers),
							Codec.BOOL.fieldOf("destroy_original").forGetter(CopyCardsAbility::destroyOriginal),
							Codec.BOOL.fieldOf("restore_health").forGetter(CopyCardsAbility::restoreHealth),
							Codec.BOOL.fieldOf("give_to_enemy").forGetter(CopyCardsAbility::giveToEnemy),
							ResourceLocation.CODEC.optionalFieldOf("animation")
									.forGetter(CopyCardsAbility::getAnimation),
							CardAbilitySelection.CODEC.fieldOf("selection").forGetter(CopyCardsAbility::getSelection))
					.apply(instance, CopyCardsAbility::new)));

	private final boolean destroyOriginal;
	private final boolean restoreHealth;
	private final boolean giveToEnemy;
	private final Optional<ResourceLocation> animation;
	private final CardAbilitySelection selection;

	public CopyCardsAbility(Set<CardAbilityTrigger> triggers, boolean destroyOriginal, boolean restoreHealth,
			boolean giveToEnemy, Optional<ResourceLocation> animation, CardAbilitySelection selection) {
		super(triggers);
		this.destroyOriginal = destroyOriginal;
		this.restoreHealth = restoreHealth;
		this.giveToEnemy = giveToEnemy;
		this.animation = animation;
		this.selection = selection;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.COPY_CARDS.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		return new Object[] { GameUtil.emphasize(GameUtil.triggersToText(getTriggers())), selection.getText(),
				new TranslatableComponent(
						ModCardAbilities.COPY_CARDS.get().getTranslationKey() + (giveToEnemy ? ".enemy" : ".you")),
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
	public void createChoices(List<Receiver> receivers, PlayerState state, Card card) {
		selection.createChoice(receivers, this, state, card);
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			ItemStack icon) {
		var selected = selection.select(state.getGame(), this, state.getId(), card, other);

		var copies = selected.stream()
				.map(c -> new Card(c.getType(), c.getCost(), c.getOriginalCost(), c.getHealth(), c.getMaxHealth(),
						c.getOriginalHealth(), c.getDamage(), c.getOriginalDamage(), new HashMap<>(c.getProperties()),
						c.getAbility(), c.getAdditionalData()))
				.collect(Collectors.toCollection(() -> new ArrayList<>()));

		for (var copy : copies)
			copy.setHealth(copy.getMaxHealth());

		if (giveToEnemy)
			state.getGame().getEnemyPlayerState(state.getId()).addCards(receivers, copies);
		else
			state.addCards(receivers, copies);

		animation.ifPresent(anim -> {
			for (var receiver : receivers) {
				receiver.receiver(new AnimationMessage(card.getId(), selected.stream().filter(c -> {
					return state.getGame().calcVisibility(receiver.getId(), c) == CardVisibility.VISIBLE
							|| state.getGame().calcVisibility(state.getId(), c) == CardVisibility.ENEMY_HAND;
				}).map(c -> c.getId()).collect(Collectors.toList()), anim));
			}
		});

		state.getGame().addHistory(receivers, new HistoryEntry(icon, state.getId(), card,
				selected.stream().filter(c -> state.getGame().isInBoard(c)).toList()));

		if (destroyOriginal)
			for (var c : selected)
				state.getGame().removeCard(receivers, c);
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

	public boolean giveToEnemy() {
		return giveToEnemy;
	}

	public Optional<ResourceLocation> getAnimation() {
		return animation;
	}

}
