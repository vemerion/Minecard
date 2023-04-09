package mod.vemerion.minecard.game;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.ability.CardAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class Card {

	private static int counter = 0;

	public static final Codec<Card> CODEC = ExtraCodecs.lazyInitializedCodec(() -> RecordCodecBuilder.create(
			instance -> instance.group(ForgeRegistries.ENTITIES.getCodec().fieldOf("entity").forGetter(Card::getType),
					Codec.INT.fieldOf("cost").forGetter(Card::getCost),
					Codec.INT.fieldOf("health").forGetter(Card::getHealth),
					Codec.INT.fieldOf("damage").forGetter(Card::getDamage),
					Codec.BOOL.fieldOf("ready").forGetter(Card::isReady),
					CardProperty.CODEC_MAP.optionalFieldOf("properties", new HashMap<>())
							.forGetter(Card::getProperties),
					CardAbility.CODEC.optionalFieldOf("abilities", NoCardAbility.NO_CARD_ABILITY)
							.forGetter(Card::getAbility),
					GameUtil.EQUIPMENT_MAP_CODEC.optionalFieldOf("equipment", new HashMap<>())
							.forGetter(Card::getEquipment),
					AdditionalCardData.CODEC.optionalFieldOf("additional_data", AdditionalCardData.EMPTY)
							.forGetter(Card::getAdditionalData))
					.apply(instance, Card::new)));

	private EntityType<?> type;
	private AdditionalCardData additionalData;
	private int cost;
	private int health;
	private int damage;
	private boolean ready;
	private Map<CardProperty, Integer> properties;
	private final CardAbility ability;
	private Map<EquipmentSlot, Item> equipment;
	private int id;

	public Card(EntityType<?> type, int cost, int health, int damage, boolean ready,
			Map<CardProperty, Integer> properties, CardAbility ability, Map<EquipmentSlot, Item> equipment,
			AdditionalCardData additionalData) {
		this.type = type;
		this.cost = cost;
		this.health = health;
		this.damage = damage;
		this.ready = ready;
		this.properties = properties;
		this.ability = ability;
		this.equipment = equipment;
		this.additionalData = additionalData;
		this.id = counter++;
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

	public void setHealth(int health) {
		this.health = health;
	}

	public void hurt(int amount) {
		if (hasProperty(CardProperty.SHIELD) && amount > 0) {
			removeProperty(CardProperty.SHIELD);
			return;
		}
		this.health -= amount;
	}

	public boolean isDead() {
		return health <= 0;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean b) {
		this.ready = b;
	}

	public Map<CardProperty, Integer> getProperties() {
		return properties;
	}

	public boolean hasProperty(CardProperty property) {
		return properties.getOrDefault(property, 0) > 0;
	}

	public void removeProperty(CardProperty property) {
		properties.remove(property);
	}

	public CardAbility getAbility() {
		return ability;
	}

	public Map<EquipmentSlot, Item> getEquipment() {
		return equipment;
	}

	public AdditionalCardData getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(AdditionalCardData data) {
		this.additionalData = data;
	}

	public int getId() {
		return id;
	}

	public Card setId(int id) {
		this.id = id;
		return this;
	}

	public void copy(Card received) {
		this.cost = received.cost;
		this.health = received.getHealth();
		this.damage = received.getDamage();
		this.ready = received.isReady();
		this.properties = received.getProperties();
		this.equipment = received.getEquipment();
		this.additionalData = received.getAdditionalData();
	}
}
