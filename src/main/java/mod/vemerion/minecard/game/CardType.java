package mod.vemerion.minecard.game;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.ability.CardAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class CardType {
	public static final Codec<CardType> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(ForgeRegistries.ENTITIES.getCodec().fieldOf("entity").forGetter(CardType::getType),
							Codec.INT.fieldOf("cost").forGetter(CardType::getCost),
							Codec.INT.fieldOf("health").forGetter(CardType::getHealth),
							Codec.INT.fieldOf("damage").forGetter(CardType::getDamage),
							CardProperty.CODEC_MAP.optionalFieldOf("properties", new HashMap<>())
									.forGetter(CardType::getProperties),
							CardAbility.CODEC.optionalFieldOf("abilities", NoCardAbility.NO_CARD_ABILITY)
									.forGetter(CardType::getAbility),
							AdditionalCardData.CODEC.optionalFieldOf("additional_data", AdditionalCardData.EMPTY)
									.forGetter(CardType::getAdditionalData))
					.apply(instance, CardType::new)));

	private final EntityType<?> type;
	private final AdditionalCardData additionalData;
	private final int cost;
	private final int health;
	private final int damage;
	private final Card cardForRendering;
	private final Map<CardProperty, Integer> properties;
	private final CardAbility ability;

	public CardType(EntityType<?> type, int cost, int health, int damage, Map<CardProperty, Integer> properties,
			CardAbility ability, AdditionalCardData additionalData) {
		this.type = type;
		this.cost = cost;
		this.health = health;
		this.damage = damage;
		this.ability = ability;
		this.additionalData = additionalData;
		this.properties = properties;
		this.cardForRendering = create();

	}

	public Card create() {
		return new Card(type, cost, health, damage, false, new HashMap<>(properties), ability, additionalData);
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

	public Map<CardProperty, Integer> getProperties() {
		return properties;
	}

	public CardAbility getAbility() {
		return ability;
	}

	public AdditionalCardData getAdditionalData() {
		return additionalData;
	}

	public Card getCardForRendering() {
		return cardForRendering;
	}
}
