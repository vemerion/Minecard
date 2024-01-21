package mod.vemerion.minecard.game.ability;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.init.ModCardVariables;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public abstract class CardVariable {

	public static final SimpleVariable COST = new CardVariable.SimpleVariable(ModCardVariables.COST, c -> c.getCost(),
			(s, c, r, v) -> c.setCost(v));
	public static final SimpleVariable DAMAGE = new CardVariable.SimpleVariable(ModCardVariables.DAMAGE,
			c -> c.getDamage(), (s, c, r, v) -> c.setDamage(v));
	public static final SimpleVariable MAX_HEALTH = new CardVariable.SimpleVariable(ModCardVariables.MAX_HEALTH,
			c -> c.getMaxHealth(), (s, c, r, v) -> {
				var diff = v - c.getMaxHealth();
				c.setMaxHealth(v);
				if (c.getHealth() > c.getMaxHealth())
					c.setHealth(v);
				else if (diff > 0)
					c.setHealth(c.getHealth() + diff);

			});
	public static final SimpleVariable HEALTH = new CardVariable.SimpleVariable(ModCardVariables.HEALTH,
			c -> c.getHealth(), (s, c, r, v) -> {
				if (c.getHealth() < 0)
					c.setHealth(0);
				if (v < 0)
					s.getGame().hurt(r, c, -v);
				else
					s.getGame().heal(r, c, v);
			});

	public static final Codec<CardVariable> CODEC = ExtraCodecs.lazyInitializedCodec(() -> ModCardVariables
			.getRegistry().getCodec().dispatch("type", CardVariable::getType, CardVariableType::codec));

	private CardVariable() {
	}

	protected abstract CardVariableType<?> getType();

	public abstract int get(Card card);

	public abstract void set(PlayerState state, Card card, List<Receiver> receivers, int value);

	public static class CardVariableType<T extends CardVariable> {
		private final Codec<T> codec;

		public CardVariableType(Codec<T> codec) {
			this.codec = codec;
		}

		Codec<T> codec() {
			return codec;
		}
	}

	public static class SimpleVariable extends CardVariable {

		private Supplier<CardVariableType<?>> type;
		private Getter getter;
		private Setter setter;

		public SimpleVariable(Supplier<CardVariableType<?>> type, Getter getter, Setter setter) {
			this.type = type;
			this.getter = getter;
			this.setter = setter;
		}

		@Override
		protected CardVariableType<?> getType() {
			return type.get();
		}

		@Override
		public int get(Card card) {
			return getter.get(card);
		}

		@Override
		public void set(PlayerState state, Card card, List<Receiver> receivers, int value) {
			setter.set(state, card, receivers, value);
		}

		public static interface Getter {
			public int get(Card card);
		}

		public static interface Setter {
			public void set(PlayerState state, Card card, List<Receiver> receivers, int value);
		}
	}

	public static class PropertyVariable extends CardVariable {

		public static final Codec<PropertyVariable> CODEC = ResourceLocation.CODEC.xmap(rl -> new PropertyVariable(rl),
				p -> p.getProperty());

		private ResourceLocation property;

		public PropertyVariable(ResourceLocation property) {
			this.property = property;
		}

		public ResourceLocation getProperty() {
			return property;
		}

		@Override
		protected CardVariableType<?> getType() {
			return ModCardVariables.PROPERTY.get();
		}

		@Override
		public int get(Card card) {
			return card.getProperty(property);
		}

		@Override
		public void set(PlayerState state, Card card, List<Receiver> receivers, int value) {
			if (property.equals(CardProperty.BABY) && value == 0 && card.hasProperty(CardProperty.BABY)) {
				card.ability(a -> a.trigger(CardAbilityTrigger.GROW, receivers, state, card, card, null));
			}
			card.putProperty(property, value);
		}
	}
}
