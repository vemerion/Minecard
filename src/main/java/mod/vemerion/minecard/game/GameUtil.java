package mod.vemerion.minecard.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.UnboundedMapCodec;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class GameUtil {

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

	public static Component propertiesToComponent(Map<CardProperty, Integer> properties) {
		var result = TextComponent.EMPTY.copy();

		for (var entry : properties.entrySet()) {
			result.append(entry.getValue() > 0 ? "+" : "-")
					.append(new TranslatableComponent(entry.getKey().getTextKey())).append(", ");
		}

		return result;
	}
}
