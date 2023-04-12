package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.LazyCardType;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.init.ModCardAbilities;
import mod.vemerion.minecard.network.AnimationMessage;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.UpdateCardMessage;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.network.PacketDistributor;

public class ModifyAbility extends CardAbility {

	public static final Codec<ModifyAbility> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(
			instance -> instance.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger),
					ResourceLocation.CODEC.optionalFieldOf("animation").forGetter(ModifyAbility::getAnimation),
					CardAbilitySelection.CODEC.fieldOf("selection").forGetter(ModifyAbility::getSelection),
					ExtraCodecs.nonEmptyList(Codec.list(LazyCardType.CODEC)).fieldOf("modifications")
							.forGetter(ModifyAbility::getModifications))
					.apply(instance, ModifyAbility::new)));

	private final Optional<ResourceLocation> animation;
	private final CardAbilitySelection selection;
	private final List<LazyCardType> modifications;

	public ModifyAbility(CardAbilityTrigger trigger, Optional<ResourceLocation> animation,
			CardAbilitySelection selection, List<LazyCardType> modifications) {
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
			var card = m.get(true);
			var damageText = modifierText(card.getDamage());
			var healthText = modifierText(card.getHealth());
			elements.append(new TranslatableComponent(ModCardAbilities.MODIFY.get().getTranslationKey() + ".element",
					GameUtil.propertiesToComponent(card.getProperties()), damageText, healthText));
		}
		return new Object[] { trigger.getText(),
				modifications.size() == 1 ? TextComponent.EMPTY
						: new TranslatableComponent(ModCardAbilities.MODIFY.get().getTranslationKey() + ".one_of"),
				elements, selection.getText() };
	}

	private TextComponent modifierText(int value) {
		return new TextComponent((value > 0 ? "+" : "") + String.valueOf(value));
	}

	@Override
	protected void invoke(List<ServerPlayer> receivers, PlayerState state, Card card, @Nullable Card other) {
		var modification = modifications.get(state.getGame().getRandom().nextInt(modifications.size())).get(false);
		if (modification == null) {
			return;
		}

		var selectedCards = selection.select(state.getGame(), state.getId(), card, other);

		for (var selected : selectedCards) {
			selected.getEquipment().putAll(modification.getEquipment());
			selected.setHealth(selected.getHealth() + modification.getHealth());
			selected.setDamage(selected.getDamage() + modification.getDamage());
			selected.setMaxHealth(selected.getMaxHealth() + modification.getHealth());
			selected.setMaxDamage(selected.getMaxDamage() + modification.getDamage());

			selected.getProperties().putAll(modification.getProperties());
			if (modification.hasProperty(CardProperty.CHARGE))
				selected.setReady(true);

			var msg = new UpdateCardMessage(selected);
			for (var receiver : receivers) {
				Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver), msg);
			}
		}

		animation.ifPresent(anim -> {
			var msg = new AnimationMessage(card.getId(),
					selectedCards.stream().map(c -> c.getId()).collect(Collectors.toList()), anim);
			for (var receiver : receivers) {
				Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver), msg);
			}
		});
	}

	private Optional<ResourceLocation> getAnimation() {
		return animation;
	}

	public List<LazyCardType> getModifications() {
		return modifications;
	}

	public CardAbilitySelection getSelection() {
		return selection;
	}

}
