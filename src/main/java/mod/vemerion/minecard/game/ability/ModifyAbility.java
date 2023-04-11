package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.LazyCardType;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.init.ModCardAbilities;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.UpdateCardMessage;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.network.PacketDistributor;

public class ModifyAbility extends CardAbility {

	public static final Codec<ModifyAbility> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger),
					Codec.BOOL.fieldOf("apply_to_target").forGetter(ModifyAbility::applyToTarget),
					ExtraCodecs.nonEmptyList(Codec.list(LazyCardType.CODEC)).fieldOf("modifications")
							.forGetter(ModifyAbility::getModifications))
					.apply(instance, ModifyAbility::new));

	private final boolean applyToTarget;
	private final List<LazyCardType> modifications;
	private final Random random = new Random();

	public ModifyAbility(CardAbilityTrigger trigger, boolean applyToTarget, List<LazyCardType> modifications) {
		super(trigger);
		this.applyToTarget = applyToTarget;
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
				elements,
				applyToTarget ? new TranslatableComponent(ModCardAbilities.MODIFY.get().getTranslationKey() + ".target")
						: TextComponent.EMPTY };
	}

	private TextComponent modifierText(int value) {
		return new TextComponent((value > 0 ? "+" : "") + String.valueOf(value));
	}

	@Override
	protected void invoke(List<ServerPlayer> receivers, PlayerState state, Card card, @Nullable Card other) {
		var modification = modifications.get(random.nextInt(modifications.size())).get(false);
		if (modification == null) {
			return;
		}

		var selected = applyToTarget ? other : card;

		if (selected == null)
			return;

		selected.getEquipment().putAll(modification.getEquipment());
		selected.setHealth(selected.getHealth() + modification.getHealth());
		selected.setDamage(selected.getDamage() + modification.getDamage());
		selected.setMaxHealth(selected.getMaxHealth() + modification.getHealth());
		selected.setMaxDamage(selected.getMaxDamage() + modification.getDamage());

		selected.getProperties().putAll(modification.getProperties());
		if (modification.hasProperty(CardProperty.CHARGE))
			selected.setReady(true);

		var msg = new UpdateCardMessage(state.getId(), selected);
		for (var receiver : receivers) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver), msg);
		}
	}

	public List<LazyCardType> getModifications() {
		return modifications;
	}

	public boolean applyToTarget() {
		return applyToTarget;
	}

}
