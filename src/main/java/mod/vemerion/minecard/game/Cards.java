package mod.vemerion.minecard.game;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class Cards {
	public static final Card EMPTY = new Card(EntityType.PIG, 0, 0, 0);

	private static Cards instance;

	private final Map<EntityType<?>, Card> CARDS;

	private Cards() {
		CARDS = new HashMap<>();
		CARDS.put(EntityType.CREEPER, new Card(EntityType.CREEPER, 3, 2, 3));
	}

	public Card get(EntityType<?> type) {
		return CARDS.computeIfAbsent(type, t -> new Card(t, 5, 5, 5));
	}

	public static Cards getInstance() {
		if (instance == null)
			instance = new Cards();
		return instance;
	}

	public static boolean isAllowed(EntityType<?> type) {
		return type.getCategory() != MobCategory.MISC;
	}
}
