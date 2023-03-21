package mod.vemerion.minecard.game;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

public class Card {

	public static final Codec<Card> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(ForgeRegistries.ENTITIES.getCodec().fieldOf("type").forGetter(Card::getType),
					Codec.INT.fieldOf("cost").forGetter(Card::getCost),
					Codec.INT.fieldOf("health").forGetter(Card::getHealth),
					Codec.INT.fieldOf("damage").forGetter(Card::getDamage),
					Codec.BOOL.fieldOf("ready").forGetter(Card::isReady)).apply(instance, Card::new));

	private final EntityType<?> type;
	private final int cost;
	private int health;
	private final int damage;
	private boolean ready;

	public Card(EntityType<?> type, int cost, int health, int damage, boolean ready) {
		this.type = type;
		this.cost = cost;
		this.health = health;
		this.damage = damage;
		this.ready = ready;
	}

	public Card copy() {
		return new Card(type, cost, health, damage, ready);
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

	public void hurt(int amount) {
		this.health -= amount;
	}

	public boolean isDead() {
		return health <= 0;
	}

	public int getDamage() {
		return damage;
	}

	public boolean isReady() {
		return ready;
	}

	public void setReady(boolean b) {
		this.ready = b;
	}
}
