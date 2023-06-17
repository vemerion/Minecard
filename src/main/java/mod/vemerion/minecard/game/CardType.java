package mod.vemerion.minecard.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.ability.CardAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class CardType {

	public static final int DEFAULT_DECK_COUNT = 3;
	public static final float DEFAULT_DROP_CHANCE = 0.1f;

	public static final Codec<CardType> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance.group(
					ForgeRegistries.ENTITIES.getCodec().fieldOf("entity").forGetter(CardType::getType),
					Codec.INT.fieldOf("cost").forGetter(CardType::getCost),
					Codec.INT.fieldOf("health").forGetter(CardType::getHealth),
					Codec.INT.fieldOf("damage").forGetter(CardType::getDamage),
					CardProperty.CODEC_MAP.optionalFieldOf("properties", new HashMap<>())
							.forGetter(CardType::getProperties),
					GameUtil.SafeOptionalCodec.defaulted("abilities", CardAbility.CODEC, NoCardAbility.NO_CARD_ABILITY)
							.forGetter(CardType::getAbility),
					AdditionalCardData.CODEC.optionalFieldOf("additional_data", AdditionalCardData.EMPTY)
							.forGetter(CardType::getAdditionalData),
					Codec.INT.optionalFieldOf("deck_count", DEFAULT_DECK_COUNT).forGetter(CardType::getDeckCount),
					Codec.FLOAT.optionalFieldOf("drop_chance", DEFAULT_DROP_CHANCE).forGetter(CardType::getDropChance))
					.apply(instance, CardType::new)));

	private final EntityType<?> type;
	private final AdditionalCardData additionalData;
	private final int cost;
	private final int health;
	private final int damage;
	private final Card cardForRendering;
	private final Map<ResourceLocation, Integer> properties;
	private final CardAbility ability;
	private final int deckCount;
	private final float dropChance;

	public CardType(EntityType<?> type, int cost, int health, int damage, Map<ResourceLocation, Integer> properties,
			CardAbility ability, AdditionalCardData additionalData, int deckCount, float dropChance) {
		this.type = type;
		this.cost = cost;
		this.health = health;
		this.damage = damage;
		this.ability = ability;
		this.additionalData = additionalData;
		this.properties = properties;
		this.deckCount = deckCount;
		this.dropChance = dropChance;
		this.cardForRendering = create();

	}

	public Card create() {
		return new Card(Optional.ofNullable(type), cost, cost, health, health, health, damage, damage, false,
				new HashMap<>(properties), ability, additionalData);
	}

	public EntityType<?> getType() {
		return type;
	}

	public int getCost() {
		return cost;
	}

	public int getHealth() {
		return health;
	}

	public int getDamage() {
		return damage;
	}

	public Map<ResourceLocation, Integer> getProperties() {
		return properties;
	}

	public boolean hasProperty(ResourceLocation property) {
		return properties.getOrDefault(property, 0) > 0;
	}

	public CardAbility getAbility() {
		return ability;
	}

	public AdditionalCardData getAdditionalData() {
		return additionalData;
	}

	public int getDeckCount() {
		return deckCount;
	}

	public float getDropChance() {
		return dropChance;
	}

	public Component getName() {
		return getAdditionalData() instanceof AdditionalCardData.ItemData itemData ? itemData.getStack().getHoverName()
				: getType().getDescription();
	}

	public Card getCardForRendering() {
		return cardForRendering;
	}
}
