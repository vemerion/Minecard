package mod.vemerion.minecard.game.ability;

import java.util.List;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;

public class ResourceAbility extends CardAbility {

	public static final Codec<ResourceAbility> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(CardAbilityTrigger.CODEC.fieldOf("trigger").forGetter(CardAbility::getTrigger),
					Codec.INT.fieldOf("temporary_resources").forGetter(ResourceAbility::getTemporaryResources),
					Codec.INT.fieldOf("permanent_resources").forGetter(ResourceAbility::getPermanentResources))
			.apply(instance, ResourceAbility::new));

	private final int temporaryResources;
	private final int permanentResources;

	public ResourceAbility(CardAbilityTrigger trigger, int temporaryResources, int permanentResources) {
		super(trigger);
		this.temporaryResources = temporaryResources;
		this.permanentResources = permanentResources;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.RESOURCE.get();
	}

	@Override
	protected Object[] getDescriptionArgs() {
		return new Object[] { trigger.getText(), temporaryResources, permanentResources };
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other) {
		state.addResources(receivers, temporaryResources, permanentResources);
		state.getGame().addHistory(receivers,
				new HistoryEntry(HistoryEntry.Type.ABILITY, state.getId(), card, List.of()));
	}

	public int getTemporaryResources() {
		return temporaryResources;
	}

	public int getPermanentResources() {
		return permanentResources;
	}

}
