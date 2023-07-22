package mod.vemerion.minecard.game.ability;

import java.util.Random;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.init.ModCardOperators;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class CardOperator {

	public static final Codec<CardOperator> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ModCardOperators
			.getRegistry().getCodec().dispatch("type", CardOperator::getType, CardOperatorType::codec));

	private Component description;

	private CardOperator() {
	}

	protected abstract Object[] getDescriptionArgs();

	protected abstract CardOperatorType<?> getType();

	public abstract int evaluate(Random rand, Card card);

	public Component getDescription() {
		if (description == null) {
			description = new TranslatableComponent(getType().getTranslationKey(), getDescriptionArgs());
		}
		return description;
	}

	public static class CardOperatorType<T extends CardOperator> extends ForgeRegistryEntry<CardOperatorType<?>> {
		private final Codec<T> codec;

		public CardOperatorType(Codec<T> codec) {
			this.codec = codec;
		}

		Codec<T> codec() {
			return codec;
		}

		public String getTranslationKey() {
			return Util.makeDescriptionId(ModCardOperators.CARD_OPERATORS.getRegistryName().getPath(),
					getRegistryName());
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
		public int evaluate(Random rand, Card card) {
			return variable.get(card);
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { variable.getDescription() };
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
		public int evaluate(Random rand, Card card) {
			return value;
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { value };
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
		public int evaluate(Random rand, Card card) {
			return rand.nextInt(min.evaluate(rand, card), max.evaluate(rand, card) + 1);
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { min.getDescription(), max.getDescription() };
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
		public int evaluate(Random rand, Card card) {
			return left.evaluate(rand, card) + right.evaluate(rand, card);
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { left.getDescription(), right.getDescription() };
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
		public int evaluate(Random rand, Card card) {
			return left.evaluate(rand, card) - right.evaluate(rand, card);
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { left.getDescription(), right.getDescription() };
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
		public int evaluate(Random rand, Card card) {
			return left.evaluate(rand, card) * right.evaluate(rand, card);
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { left.getDescription(), right.getDescription() };
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
		public int evaluate(Random rand, Card card) {
			return left.evaluate(rand, card) > right.evaluate(rand, card) ? 1 : 0;
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { left.getDescription(), right.getDescription() };
		}

	}
}
