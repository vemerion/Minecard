package mod.vemerion.minecard.game;

import net.minecraft.world.entity.EntityType;

public class Card {

	private final EntityType<?> type;
	private final int cost;
	private final int health;
	private final int damage;

	public Card(EntityType<?> type, int cost, int health, int damage) {
		this.type = type;
		this.cost = cost;
		this.health = health;
		this.damage = damage;
	}

	public Card copy() {
		return new Card(type, cost, health, damage);
	}
	
	public EntityType<?> getType() {
		return type;
	}
}
