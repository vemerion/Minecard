package mod.vemerion.minecard.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class Cards {
	public static final CardType EMPTY_CARD_TYPE = new CardType(null, 0, 0, 0, AdditionalCardData.EMPTY);
	public static final Card EMPTY_CARD = EMPTY_CARD_TYPE.create();
	public static final CardType PLAYER = new CardType(EntityType.PLAYER, 0, 30, 0, AdditionalCardData.EMPTY);

	private static Cards instance;

	private final Map<ResourceLocation, CardType> CARDS;

	private Cards() {
		CARDS = new HashMap<>();
		CARDS.put(EntityType.CREEPER.getRegistryName(),
				new CardType(EntityType.CREEPER, 3, 2, 3, AdditionalCardData.EMPTY));
	}

	public CardType get(EntityType<?> type) {
		return CARDS.computeIfAbsent(type.getRegistryName(), rl -> generateCardType(type));
	}

	public static Cards getInstance() {
		if (instance == null)
			instance = new Cards();
		return instance;
	}

	public static boolean isAllowed(EntityType<?> type) {
		return type.getCategory() != MobCategory.MISC;
	}

	private CardType generateCardType(EntityType<?> type) {
		Random rand = new Random(type.getRegistryName().toString().hashCode());

		int cost = rand.nextInt(1, 11);
		int totalStats = cost * 2 + 1;
		int health = rand.nextInt(1, totalStats);

		return new CardType(type, cost, health, totalStats - health, AdditionalCardData.EMPTY);
	}
}
