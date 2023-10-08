package mod.vemerion.minecard.game.ability;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.init.ModCardOperators;
import net.minecraft.util.ExtraCodecs;

public abstract class CardOperator {

	public static final Codec<CardOperator> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ModCardOperators
			.getRegistry().getCodec().dispatch("type", CardOperator::getType, CardOperatorType::codec));

	private CardOperator() {
	}

	protected abstract CardOperatorType<?> getType();

	public abstract int evaluate(Random rand, Card card, Collected collected);

	public static class CardOperatorType<T extends CardOperator> {
		private final Codec<T> codec;

		public CardOperatorType(Codec<T> codec) {
			this.codec = codec;
		}

		Codec<T> codec() {
			return codec;
		}
	}

	public static class Variable extends CardOperator {

		public static final Codec<Variable> CODEC = CardVariable.CODEC.xmap(v -> new Variable(v), v -> v.getVariable());

		private CardVariable variable;

		public Variable(CardVariable variable) {
			this.variable = variable;
		}

		@Override
		protected CardOperatorType<?> getType() {
			return ModCardOperators.VARIABLE.get();
		}

		public CardVariable getVariable() {
			return variable;
		}

		@Override
		public int evaluate(Random rand, Card card, Collected collected) {
			return variable.get(card);
		}
	}

	public static class Constant extends CardOperator {

		public static final Codec<Constant> CODEC = Codec.INT.xmap(i -> new Constant(i), c -> c.getValue());

		private int value;

		public Constant(int value) {
			this.value = value;
		}

		@Override
		protected CardOperatorType<?> getType() {
			return ModCardOperators.CONSTANT.get();
		}

		public int getValue() {
			return value;
		}

		@Override
		public int evaluate(Random rand, Card card, Collected collected) {
			return value;
		}
	}

	public static class RandomOperator extends CardOperator {

		public static final Codec<RandomOperator> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(CardOperator.CODEC.fieldOf("min").forGetter(RandomOperator::getMin),
								CardOperator.CODEC.fieldOf("max").forGetter(RandomOperator::getMax))
						.apply(instance, RandomOperator::new)));

		private CardOperator min;
		private CardOperator max;

		public RandomOperator(CardOperator min, CardOperator max) {
			this.min = min;
			this.max = max;
		}

		@Override
		protected CardOperatorType<?> getType() {
			return ModCardOperators.RANDOM.get();
		}

		public CardOperator getMin() {
			return min;
		}

		public CardOperator getMax() {
			return max;
		}

		@Override
		public int evaluate(Random rand, Card card, Collected collected) {
			return rand.nextInt(min.evaluate(rand, card, collected), max.evaluate(rand, card, collected) + 1);
		}
	}

	public static class Add extends CardOperator {

		public static final Codec<Add> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(CardOperator.CODEC.fieldOf("left").forGetter(Add::getLeft),
								CardOperator.CODEC.fieldOf("right").forGetter(Add::getRight))
						.apply(instance, Add::new)));

		private CardOperator left;
		private CardOperator right;

		public Add(CardOperator left, CardOperator right) {
			this.left = left;
			this.right = right;
		}

		@Override
		protected CardOperatorType<?> getType() {
			return ModCardOperators.ADD.get();
		}

		public CardOperator getLeft() {
			return left;
		}

		public CardOperator getRight() {
			return right;
		}

		@Override
		public int evaluate(Random rand, Card card, Collected collected) {
			return left.evaluate(rand, card, collected) + right.evaluate(rand, card, collected);
		}
	}

	public static class Sub extends CardOperator {

		public static final Codec<Sub> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(CardOperator.CODEC.fieldOf("left").forGetter(Sub::getLeft),
								CardOperator.CODEC.fieldOf("right").forGetter(Sub::getRight))
						.apply(instance, Sub::new)));

		private CardOperator left;
		private CardOperator right;

		public Sub(CardOperator left, CardOperator right) {
			this.left = left;
			this.right = right;
		}

		@Override
		protected CardOperatorType<?> getType() {
			return ModCardOperators.SUB.get();
		}

		public CardOperator getLeft() {
			return left;
		}

		public CardOperator getRight() {
			return right;
		}

		@Override
		public int evaluate(Random rand, Card card, Collected collected) {
			return left.evaluate(rand, card, collected) - right.evaluate(rand, card, collected);
		}
	}

	public static class Mul extends CardOperator {

		public static final Codec<Mul> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(CardOperator.CODEC.fieldOf("left").forGetter(Mul::getLeft),
								CardOperator.CODEC.fieldOf("right").forGetter(Mul::getRight))
						.apply(instance, Mul::new)));

		private CardOperator left;
		private CardOperator right;

		public Mul(CardOperator left, CardOperator right) {
			this.left = left;
			this.right = right;
		}

		@Override
		protected CardOperatorType<?> getType() {
			return ModCardOperators.MUL.get();
		}

		public CardOperator getLeft() {
			return left;
		}

		public CardOperator getRight() {
			return right;
		}

		@Override
		public int evaluate(Random rand, Card card, Collected collected) {
			return left.evaluate(rand, card, collected) * right.evaluate(rand, card, collected);
		}
	}

	public static class GreaterThan extends CardOperator {

		public static final Codec<GreaterThan> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(CardOperator.CODEC.fieldOf("left").forGetter(GreaterThan::getLeft),
								CardOperator.CODEC.fieldOf("right").forGetter(GreaterThan::getRight))
						.apply(instance, GreaterThan::new)));

		private CardOperator left;
		private CardOperator right;

		public GreaterThan(CardOperator left, CardOperator right) {
			this.left = left;
			this.right = right;
		}

		@Override
		protected CardOperatorType<?> getType() {
			return ModCardOperators.GREATER_THAN.get();
		}

		public CardOperator getLeft() {
			return left;
		}

		public CardOperator getRight() {
			return right;
		}

		@Override
		public int evaluate(Random rand, Card card, Collected collected) {
			return left.evaluate(rand, card, collected) > right.evaluate(rand, card, collected) ? 1 : 0;
		}
	}

	public static class Negate extends CardOperator {

		public static final Codec<Negate> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder
				.create(instance -> instance.group(CardOperator.CODEC.fieldOf("inner").forGetter(Negate::getInner))
						.apply(instance, Negate::new)));

		private CardOperator inner;

		public Negate(CardOperator inner) {
			this.inner = inner;
		}

		@Override
		protected CardOperatorType<?> getType() {
			return ModCardOperators.NEGATE.get();
		}

		public CardOperator getInner() {
			return inner;
		}

		@Override
		public int evaluate(Random rand, Card card, Collected collected) {
			return -inner.evaluate(rand, card, collected);
		}
	}

	public static class CollectedCount extends CardOperator {

		public static final Codec<CollectedCount> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder
				.create(instance -> instance.group(Codec.INT.fieldOf("index").forGetter(CollectedCount::getIndex))
						.apply(instance, CollectedCount::new)));

		private int index;

		public CollectedCount(int index) {
			this.index = index;
		}

		@Override
		protected CardOperatorType<?> getType() {
			return ModCardOperators.COLLECTED_COUNT.get();
		}

		public int getIndex() {
			return index;
		}

		@Override
		public int evaluate(Random rand, Card card, Collected collected) {
			return collected.get(index).size();
		}
	}

	public static class CollectedAny extends CardOperator {

		public static final Codec<CollectedAny> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(Codec.INT.fieldOf("index").forGetter(CollectedAny::getIndex),
								CardOperator.CODEC.fieldOf("inner").forGetter(CollectedAny::getInner))
						.apply(instance, CollectedAny::new)));

		private int index;
		private CardOperator inner;

		public CollectedAny(int index, CardOperator inner) {
			this.index = index;
			this.inner = inner;
		}

		@Override
		protected CardOperatorType<?> getType() {
			return ModCardOperators.COLLECTED_ANY.get();
		}

		public int getIndex() {
			return index;
		}

		public CardOperator getInner() {
			return inner;
		}

		@Override
		public int evaluate(Random rand, Card card, Collected collected) {
			var coll = collected.get(index);
			return coll.isEmpty() ? 0 : inner.evaluate(rand, coll.get(0), collected);
		}
	}
}
