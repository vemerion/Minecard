package mod.vemerion.minecard.game;

import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.Main;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.Lazy;

public enum CardProperty {
	TAUNT("taunt", Lazy.of(() -> new ItemStack(Items.CARROT_ON_A_STICK))),
	CHARGE("charge", Lazy.of(() -> new ItemStack(Items.SUGAR))),
	STEALTH("stealth", Lazy.of(() -> new ItemStack(Items.TALL_GRASS))),
	FREEZE("freeze", Lazy.of(() -> new ItemStack(Items.ICE))),
	SHIELD("shield", Lazy.of(() -> new ItemStack(Items.DIAMOND_CHESTPLATE))),
	BURN("burn", Lazy.of(() -> new ItemStack(Items.LAVA_BUCKET))),
	SPECIAL("special", Lazy.of(() -> new ItemStack(Items.ENCHANTED_GOLDEN_APPLE))),
	BABY("baby", Lazy.of(() -> new ItemStack(Items.EGG))),
	THORNS("thorns", Lazy.of(() -> new ItemStack(Items.POINTED_DRIPSTONE)));

	public static final Codec<Map<CardProperty, Integer>> CODEC_MAP = GameUtil
			.toMutable(Codec.unboundedMap(GameUtil.enumCodec(CardProperty.class, CardProperty::getName), Codec.INT));

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

	public String getTextKey() {
		return "card_property." + Main.MODID + "." + getName();
	}
}
