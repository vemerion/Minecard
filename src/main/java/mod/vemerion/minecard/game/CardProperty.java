package mod.vemerion.minecard.game;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.ability.CardAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class CardProperty {
	public static final ResourceLocation TAUNT = new ResourceLocation(Main.MODID, "taunt");
	public static final ResourceLocation CHARGE = new ResourceLocation(Main.MODID, "charge");
	public static final ResourceLocation STEALTH = new ResourceLocation(Main.MODID, "stealth");
	public static final ResourceLocation FREEZE = new ResourceLocation(Main.MODID, "freeze");
	public static final ResourceLocation SHIELD = new ResourceLocation(Main.MODID, "shield");
	public static final ResourceLocation BURN = new ResourceLocation(Main.MODID, "burn");
	public static final ResourceLocation SPECIAL = new ResourceLocation(Main.MODID, "special");
	public static final ResourceLocation BABY = new ResourceLocation(Main.MODID, "baby");
	public static final ResourceLocation THORNS = new ResourceLocation(Main.MODID, "thorns");
	public static final ResourceLocation POISON = new ResourceLocation(Main.MODID, "poison");
	public static final ResourceLocation UNDEAD = new ResourceLocation(Main.MODID, "undead");

	public static final Codec<CardProperty> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder
							.create(instance -> instance
									.group(ItemStack.CODEC.fieldOf("item").forGetter(CardProperty::getItem),
											GameUtil.SafeOptionalCodec
													.defaulted("ability", CardAbility.CODEC,
															NoCardAbility.NO_CARD_ABILITY)
													.forGetter(CardProperty::getAbility))
									.apply(instance, CardProperty::new)));

	public static final Codec<Map<ResourceLocation, Integer>> CODEC_MAP = GameUtil
			.toMutable(Codec.unboundedMap(ResourceLocation.CODEC, Codec.INT));

	private ItemStack item;
	private CardAbility ability;

	public CardProperty(ItemStack item, CardAbility ability) {
		this.item = item;
		this.ability = ability;
	}

	public ItemStack getItem() {
		return item;
	}

	public CardAbility getAbility() {
		return ability;
	}

	public static String getTextKey(ResourceLocation rl) {
		return "card_property." + rl.getNamespace() + "." + rl.getPath();
	}

	public static String getDescriptionKey(ResourceLocation rl) {
		return "card_property." + rl.getNamespace() + "." + rl.getPath() + ".description";
	}
}
