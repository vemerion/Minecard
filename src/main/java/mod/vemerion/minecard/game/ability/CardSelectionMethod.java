package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.GameState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardSelectionMethods;
import net.minecraft.util.ExtraCodecs;

public abstract class CardSelectionMethod {

	public static final Codec<CardSelectionMethod> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> ModCardSelectionMethods.getRegistry().getCodec().dispatch("type",
					CardSelectionMethod::getType, CardSelectionMethodType::codec));

	private CardSelectionMethod() {
	}

	protected abstract CardSelectionMethodType<?> getType();

	public abstract List<Card> select(List<Receiver> receivers, GameState state, CardAbility ability,
			List<Card> candidates);

	public static class CardSelectionMethodType<T extends CardSelectionMethod> {
		private final Codec<T> codec;

		public CardSelectionMethodType(Codec<T> codec) {
			this.codec = codec;
		}

		Codec<T> codec() {
			return codec;
		}
	}

	public static class All extends CardSelectionMethod {

		public static final All ALL = new All();

		public static final Codec<All> CODEC = Codec.unit(ALL);

		public All() {
		}

		@Override
		protected CardSelectionMethodType<?> getType() {
			return ModCardSelectionMethods.ALL.get();
		}

		@Override
		public List<Card> select(List<Receiver> receivers, GameState state, CardAbility ability,
				List<Card> candidates) {
			return candidates;
		}
	}

	public static class Random extends CardSelectionMethod {

		public static final Codec<Random> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(Codec.INT.fieldOf("count").forGetter(Random::getCount),
								Codec.BOOL.fieldOf("repeat").forGetter(Random::getRepeat))
						.apply(instance, Random::new)));

		public final int count;
		public final boolean repeat;

		public Random(int count, boolean repeat) {
			this.count = count;
			this.repeat = repeat;
		}

		@Override
		protected CardSelectionMethodType<?> getType() {
			return ModCardSelectionMethods.RANDOM.get();
		}

		public int getCount() {
			return count;
		}

		public boolean getRepeat() {
			return repeat;
		}

		@Override
		public List<Card> select(List<Receiver> receivers, GameState state, CardAbility ability,
				List<Card> candidates) {
			var result = new ArrayList<Card>();
			for (int i = 0; i < count; i++) {
				if (candidates.isEmpty())
					break;
				var picked = state.getRandom().nextInt(candidates.size());
				result.add(candidates.get(picked));
				if (!repeat) {
					candidates.remove(picked);
				}
			}
			return result;
		}
	}

	public static class Choice extends CardSelectionMethod {

		public static final Codec<Choice> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder
				.create(instance -> instance.group(Codec.BOOL.fieldOf("discover").forGetter(Choice::isDiscover))
						.apply(instance, Choice::new)));

		public final boolean discover;

		public Choice(boolean discover) {
			this.discover = discover;
		}

		@Override
		protected CardSelectionMethodType<?> getType() {
			return ModCardSelectionMethods.CHOICE.get();
		}

		@Override
		public List<Card> select(List<Receiver> receivers, GameState state, CardAbility ability,
				List<Card> candidates) {
			var result = new ArrayList<Card>();
			state.getChoice().make(receivers, ability, candidates, !discover, state.getRandom(),
					state.getCurrentPlayerState().getId()).ifPresent(c -> result.add(c));
			return result;
		}

		public boolean isDiscover() {
			return discover;
		}
	}
}
