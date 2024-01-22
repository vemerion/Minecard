package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.init.ModCardConditions;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public abstract class CardCondition {

	public static final Codec<CardCondition> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ModCardConditions
			.getRegistry().getCodec().dispatch("type", CardCondition::getType, CardConditionType::codec));

	private CardCondition() {
	}

	protected abstract CardConditionType<?> getType();

	public boolean isEmpty() {
		return false;
	}

	public abstract boolean test(Card t, Collected collected, Random rand);

	public List<Card> filter(List<Card> cards, Collected collected, Random rand) {
		return cards.stream().filter(c -> test(c, collected, rand))
				.collect(Collectors.toCollection(() -> new ArrayList<>()));
	}

	public static class CardConditionType<T extends CardCondition> {
		private final Codec<T> codec;

		public CardConditionType(Codec<T> codec) {
			this.codec = codec;
		}

		Codec<T> codec() {
			return codec;
		}
	}

	public static class NoCondition extends CardCondition {

		public static final NoCondition NO_CONDITION = new NoCondition();

		public static final Codec<NoCondition> CODEC = Codec.unit(NO_CONDITION);

		public NoCondition() {
		}

		@Override
		public boolean test(Card t, Collected collected, Random rand) {
			return true;
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.NO_CONDITION.get();
		}

		public boolean isEmpty() {
			return true;
		}
	}

	public static class And extends CardCondition {

		public static final Codec<And> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(CardCondition.CODEC.fieldOf("left").forGetter(And::getLeft),
								CardCondition.CODEC.fieldOf("right").forGetter(And::getRight))
						.apply(instance, And::new)));

		private final CardCondition left, right;

		public And(CardCondition left, CardCondition right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public boolean test(Card t, Collected collected, Random rand) {
			return left.test(t, collected, rand) && right.test(t, collected, rand);
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.AND.get();
		}

		public CardCondition getLeft() {
			return left;
		}

		public CardCondition getRight() {
			return right;
		}
	}

	public static class Or extends CardCondition {

		public static final Codec<Or> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(CardCondition.CODEC.fieldOf("left").forGetter(Or::getLeft),
								CardCondition.CODEC.fieldOf("right").forGetter(Or::getRight))
						.apply(instance, Or::new)));

		private final CardCondition left, right;

		public Or(CardCondition left, CardCondition right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public boolean test(Card t, Collected collected, Random rand) {
			return left.test(t, collected, rand) || right.test(t, collected, rand);
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.OR.get();
		}

		public CardCondition getLeft() {
			return left;
		}

		public CardCondition getRight() {
			return right;
		}
	}

	public static class Not extends CardCondition {

		public static final Codec<Not> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder
				.create(instance -> instance.group(CardCondition.CODEC.fieldOf("inner").forGetter(Not::getInner))
						.apply(instance, Not::new)));

		private final CardCondition inner;

		public Not(CardCondition inner) {
			this.inner = inner;
		}

		@Override
		public boolean test(Card t, Collected collected, Random rand) {
			return !inner.test(t, collected, rand);
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.NOT.get();
		}

		public CardCondition getInner() {
			return inner;
		}
	}

	public static class Entity extends CardCondition {

		public static final Codec<Entity> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("inner").forGetter(Entity::getEntity))
						.apply(instance, Entity::new)));

		private final EntityType<?> entity;

		public Entity(EntityType<?> entity) {
			this.entity = entity;
		}

		@Override
		public boolean test(Card t, Collected collected, Random rand) {
			return t.getType().orElse(null) == entity;
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.ENTITY.get();
		}

		public EntityType<?> getEntity() {
			return entity;
		}
	}

	public static class OperatorCondition extends CardCondition {

		public static final Codec<OperatorCondition> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(CardOperator.CODEC.fieldOf("operator").forGetter(OperatorCondition::getOperator))
						.apply(instance, OperatorCondition::new)));

		private final CardOperator operator;

		public OperatorCondition(CardOperator operator) {
			this.operator = operator;
		}

		@Override
		public boolean test(Card t, Collected collected, Random rand) {
			return operator.evaluate(rand, t, collected) > 0;
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.OPERATOR.get();
		}

		public CardOperator getOperator() {
			return operator;
		}
	}

	public static class IsSpell extends CardCondition {

		public static final IsSpell IS_SPELL = new IsSpell();

		public static final Codec<IsSpell> CODEC = Codec.unit(IS_SPELL);

		private IsSpell() {
		}

		@Override
		public boolean test(Card t, Collected collected, Random rand) {
			return t.isSpell();
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.IS_SPELL.get();
		}
	}

	public static class Contains extends CardCondition {

		public static final Codec<Contains> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder
				.create(instance -> instance.group(Codec.INT.fieldOf("index").forGetter(Contains::getIndex))
						.apply(instance, Contains::new)));

		private final int index;

		public Contains(int index) {
			this.index = index;
		}

		@Override
		public boolean test(Card t, Collected collected, Random rand) {
			return collected.get(index).contains(t);
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.CONTAINS.get();
		}

		public int getIndex() {
			return index;
		}
	}
}
