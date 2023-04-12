package mod.vemerion.minecard.datagen;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.AdditionalCardData;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.CardType;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.LazyCardType;
import mod.vemerion.minecard.game.ability.AddCardsAbility;
import mod.vemerion.minecard.game.ability.CardAbility;
import mod.vemerion.minecard.game.ability.CardAbilityGroup;
import mod.vemerion.minecard.game.ability.CardAbilitySelection;
import mod.vemerion.minecard.game.ability.CardAbilityTrigger;
import mod.vemerion.minecard.game.ability.CardCondition;
import mod.vemerion.minecard.game.ability.CardSelectionMethod;
import mod.vemerion.minecard.game.ability.CopyCardsAbility;
import mod.vemerion.minecard.game.ability.DrawCardsAbility;
import mod.vemerion.minecard.game.ability.ModifyAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import mod.vemerion.minecard.game.ability.ResourceAbility;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

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
		// Entity card
		add(new Builder(EntityType.PLAYER, 0, 30, 0));
		add(new Builder(EntityType.CREEPER, 5, 4, 3).addProperty(CardProperty.CHARGE, 1)
				.addProperty(CardProperty.FREEZE, 1).addProperty(CardProperty.STEALTH, 1)
				.addProperty(CardProperty.TAUNT, 1));
		add(new Builder(EntityType.SHULKER, 3, 3, 3).addProperty(CardProperty.SHIELD, 1));
		add(new Builder(EntityType.DONKEY, 3, 2, 2).setCardAbility(new DrawCardsAbility(CardAbilityTrigger.SUMMON, 1)));
		add(new Builder(EntityType.ZOMBIE, 4, 4, 4)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.empty(),
						new CardAbilitySelection(CardAbilityGroup.SELF, CardSelectionMethod.ALL,
								CardCondition.NoCondition.NO_CONDITION),
						List.of(new LazyCardType(mod("shield")), new LazyCardType(mod("iron_equipment")),
								new LazyCardType(mod("diamond_sword"))))));
		add(new Builder(EntityType.STRAY, 2, 2, 2)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.empty(),
						new CardAbilitySelection(CardAbilityGroup.TARGET, CardSelectionMethod.ALL,
								CardCondition.NoCondition.NO_CONDITION),
						List.of(new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.FREEZE, 1).build())))));
		add(new Builder(EntityType.VINDICATOR, 6, 4, 8).setCardAbility(new AddCardsAbility(CardAbilityTrigger.DEATH,
				new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
						.setCardAbility(new ResourceAbility(CardAbilityTrigger.SUMMON, 1, 0))
						.setAdditionalData(new AdditionalCardData.ItemData(Items.EMERALD)).build()))));
		add(new Builder(EntityType.ENDERMAN, 6, 4, 5).setCardAbility(
				new CopyCardsAbility(CardAbilityTrigger.SUMMON, new CardAbilitySelection(CardAbilityGroup.ENEMY_HAND,
						CardSelectionMethod.RANDOM, CardCondition.NoCondition.NO_CONDITION))));
		add(new Builder(EntityType.GLOW_SQUID, 3, 2, 2).setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON,
				Optional.of(new ResourceLocation(Main.MODID, "throw_carrot")),
				new CardAbilitySelection(CardAbilityGroup.BOARD, CardSelectionMethod.ALL,
						CardCondition.NoCondition.NO_CONDITION),
				List.of(new LazyCardType(
						new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.STEALTH, 0).build())))));

		// Auxiliary cards
		add(new Builder(EntityType.ITEM, 0, 1, 0).setKey(mod("shield")).addProperty(CardProperty.SHIELD, 1)
				.addEquipment(EquipmentSlot.OFFHAND, Items.SHIELD));
		add(new Builder(EntityType.ITEM, 0, 1, 1).setKey(mod("iron_equipment"))
				.addEquipment(EquipmentSlot.HEAD, Items.IRON_HELMET)
				.addEquipment(EquipmentSlot.MAINHAND, Items.IRON_SHOVEL));
		add(new Builder(EntityType.ITEM, 0, 0, 1).setKey(mod("diamond_sword")).addProperty(CardProperty.CHARGE, 1)
				.addEquipment(EquipmentSlot.MAINHAND, Items.DIAMOND_SWORD));
	}

	private ResourceLocation mod(String name) {
		return new ResourceLocation(Main.MODID, name);
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
		private CardAbility ability = NoCardAbility.NO_CARD_ABILITY;
		private Map<EquipmentSlot, Item> equipment = new HashMap<>();
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

		private Builder addEquipment(EquipmentSlot slot, Item item) {
			equipment.put(slot, item);
			return this;
		}

		private Builder setCardAbility(CardAbility ability) {
			this.ability = ability;
			return this;
		}

		private Builder setAdditionalData(AdditionalCardData data) {
			this.additionalData = data;
			return this;
		}

		private CardType build() {
			return new CardType(type, cost, health, damage, properties, ability, equipment, additionalData);
		}

		private ResourceLocation getKey() {
			return key == null ? type.getRegistryName() : key;
		}
	}

}
