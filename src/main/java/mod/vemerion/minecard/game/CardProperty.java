package mod.vemerion.minecard.game;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.Lazy;

public enum CardProperty {
	TAUNT("taunt", Lazy.of(() -> new ItemStack(Items.CARROT_ON_A_STICK))),
	CHARGE("charge", Lazy.of(() -> new ItemStack(Items.SUGAR))),
	STEALTH("stealth", Lazy.of(() -> new ItemStack(Items.TALL_GRASS))),
	FREEZE("freeze", Lazy.of(() -> new ItemStack(Items.ICE))),
	SHIELD("shield", Lazy.of(() -> new ItemStack(Items.DIAMOND_CHESTPLATE)));

	public static final Codec<CardProperty> CODEC = Codec.STRING.comapFlatMap(s -> {
		for (var e : CardProperty.values()) {
			if (e.name.equals(s))
				return DataResult.success(e);
		}
		return DataResult.error("Invalid property '" + s + "'");
	}, e -> e.name);

	// Use xmap to make sure we get modifiable map
	public static final Codec<Map<CardProperty, Integer>> CODEC_MAP = Codec.unboundedMap(CardProperty.CODEC, Codec.INT)
			.xmap(map -> new HashMap<>(map), map -> map);

	private String name;
	private Supplier<ItemStack> icon;

	private CardProperty(String name, Supplier<ItemStack> icon) {
		this.name = name;
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public ItemStack getIcon() {
		return icon.get();
	}
}
