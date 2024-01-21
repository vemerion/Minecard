package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModAdvancements;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;

public class TriggerAdvancementAbility extends CardAbility {

	public static final Codec<TriggerAdvancementAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder.create(instance -> instance
							.group(ResourceLocation.CODEC.fieldOf("id").forGetter(TriggerAdvancementAbility::getId),
									CardCondition.CODEC.fieldOf("condition")
											.forGetter(TriggerAdvancementAbility::getCondition))
							.apply(instance, TriggerAdvancementAbility::new)));

	private final ResourceLocation id;
	private final CardCondition condition;

	public TriggerAdvancementAbility(ResourceLocation id, CardCondition condition) {
		super(Set.of(), "");
		this.id = id;
		this.condition = condition;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.TRIGGER_ADVANCEMENT.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, Card cause, @Nullable Card target,
			Collected collected) {
		if (condition.test(card, collected)
				&& state.getGame().getLevel().getPlayerByUUID(state.getId()) instanceof ServerPlayer player) {
			ModAdvancements.GAME.trigger(player, id);
		}
	}

	public ResourceLocation getId() {
		return id;
	}

	public CardCondition getCondition() {
		return condition;
	}

}
