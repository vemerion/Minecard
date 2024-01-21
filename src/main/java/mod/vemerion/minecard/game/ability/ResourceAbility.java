package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;

public class ResourceAbility extends CardAbility {

	public static final Codec<ResourceAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(GameUtil.TRIGGERS_CODEC.fieldOf("triggers").forGetter(CardAbility::getTriggers),
					Codec.STRING.fieldOf("text_key").forGetter(CardAbility::getTextKey),
					Codec.INT.fieldOf("temporary_resources").forGetter(ResourceAbility::getTemporaryResources),
					Codec.INT.fieldOf("permanent_resources").forGetter(ResourceAbility::getPermanentResources))
			.apply(instance, ResourceAbility::new));

	private final int temporaryResources;
	private final int permanentResources;

	public ResourceAbility(Set<CardAbilityTrigger> triggers, String textKey, int temporaryResources,
			int permanentResources) {
		super(triggers, textKey);
		this.temporaryResources = temporaryResources;
		this.permanentResources = permanentResources;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.RESOURCE.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, Card cause, @Nullable Card target,
			Collected collected) {
		state.addResources(receivers, temporaryResources, permanentResources);
	}

	public int getTemporaryResources() {
		return temporaryResources;
	}

	public int getPermanentResources() {
		return permanentResources;
	}

}
