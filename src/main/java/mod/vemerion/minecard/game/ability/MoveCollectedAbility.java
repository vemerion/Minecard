package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardAbilities;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class MoveCollectedAbility extends CardAbility {

	public static final Codec<MoveCollectedAbility> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(Codec.INT.fieldOf("source").forGetter(MoveCollectedAbility::getSource),
							Codec.INT.fieldOf("destination").forGetter(MoveCollectedAbility::getDestination),
							Codec.BOOL.fieldOf("clear_source").forGetter(MoveCollectedAbility::shouldClearSource),
							Codec.BOOL.fieldOf("clear_destination")
									.forGetter(MoveCollectedAbility::shouldClearDestination))
					.apply(instance, MoveCollectedAbility::new)));

	private final int source;
	private final int destination;
	private final boolean clearSource;
	private final boolean clearDestination;

	public MoveCollectedAbility(int source, int destination, boolean clearSource, boolean clearDestination) {
		super(Set.of());
		this.source = source;
		this.destination = destination;
		this.clearSource = clearSource;
		this.clearDestination = clearDestination;
	}

	@Override
	protected CardAbilityType<?> getType() {
		return ModCardAbilities.MOVE_COLLECTED.get();
	}

	@Override
	protected void invoke(List<Receiver> receivers, PlayerState state, Card card, @Nullable Card other,
			Collected collected, ItemStack icon) {
		if (clearDestination) {
			collected.clear(destination);
		}
		collected.get(destination).addAll(collected.get(source));
		if (clearSource) {
			collected.clear(source);
		}
	}

	public int getSource() {
		return source;
	}

	public int getDestination() {
		return destination;
	}

	public boolean shouldClearSource() {
		return clearSource;
	}

	public boolean shouldClearDestination() {
		return clearDestination;
	}
}
