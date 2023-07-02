package mod.vemerion.minecard.game;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.OptionalFieldCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;

import mod.vemerion.minecard.game.ability.CardAbilityTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class GameUtil {

	// Does NOT silently fail if parsing fails
	public static class SafeOptionalCodec<A> extends OptionalFieldCodec<A> {

		private final String name;
		private final Codec<A> codec;

		private SafeOptionalCodec(String name, Codec<A> codec) {
			super(name, codec);
			this.name = name;
			this.codec = codec;
		}

		public static <A> MapCodec<A> defaulted(String name, Codec<A> codec, A defaultValue) {
			return new SafeOptionalCodec<A>(name, codec).xmap(optional -> optional.orElse(defaultValue),
					a -> Objects.equals(a, defaultValue) ? Optional.empty() : Optional.of(a));
		}

		@Override
		public <T> DataResult<Optional<A>> decode(DynamicOps<T> ops, MapLike<T> input) {
			var value = input.get(name);
			if (value == null)
				return DataResult.success(Optional.empty());
			return codec.parse(ops, value).map(Optional::of);
		}

	}

	public static final Codec<Set<CardAbilityTrigger>> TRIGGERS_CODEC = Codec.list(CardAbilityTrigger.CODEC)
			.comapFlatMap(list -> {
				Set<CardAbilityTrigger> set = EnumSet.copyOf(list);
				if (list.size() != set.size()) {
					return DataResult.error("Trigger list has duplicate entries");
				}
				return DataResult.success(set);
			}, set -> List.copyOf(set));

	public static Component triggersToText(Set<CardAbilityTrigger> triggers) {
		var text = TextComponent.EMPTY.copy();
		if (triggers.isEmpty())
			return text;

		boolean first = true;
		for (var trigger : triggers) {
			if (!first) {
				text.append("/");
			}
			text.append(trigger.getText());
			first = false;
		}
		text.append(": ");
		return text;
	}

	public static boolean canBeAttacked(Card card, List<? extends Card> board) {
		if (card.hasProperty(CardProperty.STEALTH) || card.isDead())
			return false;

		return card.hasProperty(CardProperty.TAUNT) || board.stream().noneMatch(
				c -> c.hasProperty(CardProperty.TAUNT) && !c.hasProperty(CardProperty.STEALTH) && !c.isDead());
	}

	public static <T extends Enum<T>> Codec<T> enumCodec(Class<T> type, Function<T, String> getName) {
		return Codec.STRING.comapFlatMap(s -> {
			for (var e : type.getEnumConstants()) {
				if (getName.apply(e).equals(s))
					return DataResult.success(e);
			}
			return DataResult.error("Invalid value '" + s + "'");
		}, e -> getName.apply(e));
	}

	public static <T, U> Codec<Map<T, U>> toMutable(UnboundedMapCodec<T, U> codec) {
		return codec.xmap(map -> new HashMap<>(map), map -> map);
	}

	public static Component emphasize(Component text) {
		return text.copy().setStyle(text.getStyle().withItalic(true));
	}
}
