package mod.vemerion.minecard.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.game.ability.CardAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

public class Card {

	private static int counter = 0;

	public static final Codec<Card> CODEC = ExtraCodecs
			.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
					.group(ForgeRegistries.ENTITIES.getCodec().optionalFieldOf("entity").forGetter(Card::getType),
							Codec.INT.fieldOf("cost").forGetter(Card::getCost),
							Codec.INT.fieldOf("original_cost").forGetter(Card::getOriginalCost),
							Codec.INT.fieldOf("health").forGetter(Card::getHealth),
							Codec.INT.fieldOf("max_health").forGetter(Card::getMaxHealth),
							Codec.INT.fieldOf("original_health").forGetter(Card::getOriginalHealth),
							Codec.INT.fieldOf("damage").forGetter(Card::getDamage),
							Codec.INT.fieldOf("original_damage").forGetter(Card::getOriginalDamage),
							CardProperty.CODEC_MAP.optionalFieldOf("properties", new HashMap<>())
									.forGetter(Card::getProperties),
							CardAbility.CODEC.optionalFieldOf("abilities", NoCardAbility.NO_CARD_ABILITY)
									.forGetter(Card::getAbility),
							AdditionalCardData.CODEC.optionalFieldOf("additional_data", AdditionalCardData.EMPTY)
									.forGetter(Card::getAdditionalData))
					.apply(instance, Card::new)));

	private Optional<EntityType<?>> type;
	private AdditionalCardData additionalData;
	private int cost;
	private int originalCost;
	private int health;
	private int maxHealth;
	private int originalHealth;
	private int damage;
	private int originalDamage;
	private Map<ResourceLocation, Integer> properties;
	private final CardAbility ability;
	private int id;
	private int textScroll; // Client only

	public Card(Optional<EntityType<?>> type, int cost, int originalCost, int health, int maxHealth, int originalHealth,
			int damage, int originalDamage, Map<ResourceLocation, Integer> properties, CardAbility ability,
			AdditionalCardData additionalData) {
		this.type = type;
		this.cost = cost;
		this.originalCost = originalCost;
		this.health = health;
		this.maxHealth = maxHealth;
		this.originalHealth = originalHealth;
		this.damage = damage;
		this.originalDamage = originalDamage;
		this.properties = properties;
		this.ability = ability;
		this.additionalData = additionalData;
		this.id = counter++;
	}

	public Card(Card other) {
		this(other.getType(), other.getCost(), other.getOriginalCost(), other.getHealth(), other.getMaxHealth(),
				other.getOriginalHealth(), other.getDamage(), other.getOriginalDamage(),
				new HashMap<>(other.getProperties()), other.getAbility(), other.getAdditionalData());
		this.id = other.getId();
	}

	public Optional<EntityType<?>> getType() {
		return type;
	}

	public int getCost() {
		return cost;
	}

	public void setCost(int cost) {
		this.cost = Math.max(0, cost);
	}

	public int getOriginalCost() {
		return originalCost;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public int getOriginalHealth() {
		return originalHealth;
	}

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = Math.max(0, maxHealth);
	}

	public boolean hurt(int amount) {
		if (hasProperty(CardProperty.SHIELD) && amount > 0) {
			removeProperty(CardProperty.SHIELD);
			return false;
		}
		this.health -= amount;
		return amount > 0;
	}

	public boolean isDead() {
		return (health <= 0 && !isSpell()) || health < 0;
	}

	public boolean isSpell() {
		return originalHealth == 0;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = Math.max(0, damage);
	}

	public int getOriginalDamage() {
		return originalDamage;
	}

	public boolean canAttack() {
		return hasProperty(CardProperty.READY) && getDamage() > 0;
	}

	public Map<ResourceLocation, Integer> getProperties() {
		return properties;
	}

	public boolean hasProperty(ResourceLocation property) {
		return getProperty(property) > 0;
	}

	public int getProperty(ResourceLocation property) {
		return properties.getOrDefault(property, 0);
	}

	public void putProperty(ResourceLocation property, int value) {
		properties.put(property, value);
	}

	public void removeProperty(ResourceLocation property) {
		properties.remove(property);
	}

	public void decrementProperty(ResourceLocation property) {
		if (hasProperty(property))
			properties.put(property, properties.get(property) - 1);
	}

	public CardAbility getAbility() {
		return ability;
	}

	public void ability(BiConsumer<CardAbility, ItemStack> func) {
		func.accept(getAbility(), new ItemStack(Items.BOOK));
		for (var property : new HashMap<>(properties).entrySet()) {
			if (property.getValue() > 0) {
				var p = CardProperties.getInstance(false).get(property.getKey());
				func.accept(p.getAbility(), p.getItem());
			}
		}
	}

	public AdditionalCardData getAdditionalData() {
		return additionalData;
	}

	public void setAdditionalData(AdditionalCardData data) {
		this.additionalData = data;
	}

	public Component getName() {
		return getAdditionalData() instanceof AdditionalCardData.ItemData itemData ? itemData.getStack().getHoverName()
				: getType().get().getDescription();
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
		this.originalCost = received.originalCost;
		this.health = received.getHealth();
		this.maxHealth = received.maxHealth;
		this.originalHealth = received.originalHealth;
		this.damage = received.getDamage();
		this.originalDamage = received.originalDamage;
		this.properties = received.getProperties();
		this.additionalData = received.getAdditionalData();
	}

	public int getTextScroll() {
		return textScroll;
	}

	public void setTextScroll(int value) {
		textScroll = value;
	}
}
