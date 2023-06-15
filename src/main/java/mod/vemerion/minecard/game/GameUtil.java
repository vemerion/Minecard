package mod.vemerion.minecard.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.codecs.OptionalFieldCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

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

	public static final Codec<Map<EquipmentSlot, Item>> EQUIPMENT_MAP_CODEC = GameUtil.toMutable(Codec.unboundedMap(
			GameUtil.enumCodec(EquipmentSlot.class, EquipmentSlot::getName), ForgeRegistries.ITEMS.getCodec()));

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

	public static Component propertiesToComponent(Map<ResourceLocation, Integer> properties) {
		var result = TextComponent.EMPTY.copy();

		for (var entry : properties.entrySet()) {
			result.append(entry.getValue() > 0 ? "+" : "-")
					.append(new TranslatableComponent(CardProperty.getTextKey(entry.getKey()))).append(", ");
		}

		return result;
	}

	public static Component emphasize(Component text) {
		return text.copy().setStyle(text.getStyle().withItalic(true));
	}
}
