package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.init.ModCardConditions;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class CardCondition implements Predicate<Card> {

	public static final Codec<CardCondition> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ModCardConditions
			.getRegistry().getCodec().dispatch("type", CardCondition::getType, CardConditionType::codec));

	private Component description;

	private CardCondition() {
	}

	protected abstract CardConditionType<?> getType();

	protected abstract Object[] getDescriptionArgs();

	public boolean isEmpty() {
		return false;
	}

	public Component getDescription() {
		if (description == null) {
			description = new TranslatableComponent(getType().getTranslationKey(), getDescriptionArgs());
		}
		return description;
	}

	public List<Card> filter(List<Card> cards) {
		return cards.stream().filter(this::test).collect(Collectors.toCollection(() -> new ArrayList<>()));
	}

	public static class CardConditionType<T extends CardCondition> extends ForgeRegistryEntry<CardConditionType<?>> {
		private final Codec<T> codec;

		public CardConditionType(Codec<T> codec) {
			this.codec = codec;
		}

		Codec<T> codec() {
			return codec;
		}

		public String getTranslationKey() {
			return Util.makeDescriptionId(ModCardConditions.CARD_CONDITIONS.getRegistryName().getNamespace(),
					getRegistryName());
		}
	}

	public static class NoCondition extends CardCondition {

		public static final NoCondition NO_CONDITION = new NoCondition();

		public static final Codec<NoCondition> CODEC = Codec.unit(NO_CONDITION);

		public NoCondition() {
		}

		@Override
		public boolean test(Card t) {
			return true;
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.NO_CONDITION.get();
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] {};
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
		public boolean test(Card t) {
			return left.test(t) && right.test(t);
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

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { left.getDescription(), right.getDescription() };
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
		public boolean test(Card t) {
			return left.test(t) || right.test(t);
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

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { left.getDescription(), right.getDescription() };
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
		public boolean test(Card t) {
			return !inner.test(t);
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.NOT.get();
		}

		public CardCondition getInner() {
			return inner;
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { inner.getDescription() };
		}
	}

	public static class Entity extends CardCondition {

		public static final Codec<Entity> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(ForgeRegistries.ENTITIES.getCodec().fieldOf("inner").forGetter(Entity::getEntity))
						.apply(instance, Entity::new)));

		private final EntityType<?> entity;

		public Entity(EntityType<?> entity) {
			this.entity = entity;
		}

		@Override
		public boolean test(Card t) {
			return t.getType().orElse(null) == entity;
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.ENTITY.get();
		}

		public EntityType<?> getEntity() {
			return entity;
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { entity.getDescription() };
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
		public boolean test(Card t) {
			return operator.evaluate(new Random(0), t) > 0;
		}

		@Override
		protected CardConditionType<?> getType() {
			return ModCardConditions.OPERATOR.get();
		}

		public CardOperator getOperator() {
			return operator;
		}

		@Override
		protected Object[] getDescriptionArgs() {
			return new Object[] { operator.getDescription() };
		}
	}
}
