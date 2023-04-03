package mod.vemerion.minecard.datagen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.AdditionalCardData;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.CardType;
import mod.vemerion.minecard.game.Cards;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

public class ModCardProvider implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private Map<ResourceLocation, CardType> cards = new HashMap<>();
	private DataGenerator generator;

	public ModCardProvider(DataGenerator generator) {
		this.generator = generator;
	}

	@Override
	public void run(HashCache cache) throws IOException {
		addCards();
		var folder = generator.getOutputFolder();
		for (var entry : cards.entrySet()) {
			var key = entry.getKey();
			var path = folder
					.resolve("data/" + key.getNamespace() + "/" + Cards.FOLDER_NAME + "/" + key.getPath() + ".json");
			try {
				DataProvider.save(GSON, cache,
						CardType.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, s -> {
						}), path);
			} catch (IOException e) {
				Main.LOGGER.error("Couldn't save card " + path + ": " + e);
			}
		}
	}

	private void addCards() {
		add(new Builder(EntityType.PLAYER, 0, 30, 0));
		add(new Builder(EntityType.CREEPER, 5, 4, 3).addProperty(CardProperty.CHARGE, 1)
				.addProperty(CardProperty.FREEZE, 1).addProperty(CardProperty.STEALTH, 1)
				.addProperty(CardProperty.TAUNT, 1));
		add(new Builder(EntityType.SHULKER, 3, 3, 3).addProperty(CardProperty.SHIELD, 1));
	}

	private void add(Builder builder) {
		cards.put(builder.getKey(), builder.build());
	}

	@Override
	public String getName() {
		return Main.MODID + ": Cards";
	}

	private static class Builder {

		private EntityType<?> type;
		private int cost;
		private int health;
		private int damage;
		private Map<CardProperty, Integer> properties = new HashMap<>();
		private AdditionalCardData additionalData = AdditionalCardData.EMPTY;
		private ResourceLocation key;

		private Builder(EntityType<?> type, int cost, int health, int damage) {
			this.type = type;
			this.cost = cost;
			this.health = health;
			this.damage = damage;
		}

		private Builder setKey(ResourceLocation key) {
			this.key = key;
			return this;
		}

		private Builder addProperty(CardProperty property, int value) {
			properties.put(property, value);
			return this;
		}

		private CardType build() {
			return new CardType(type, cost, health, damage, properties, additionalData);
		}

		private ResourceLocation getKey() {
			return key == null ? type.getRegistryName() : key;
		}
	}

}
