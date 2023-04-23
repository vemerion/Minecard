package mod.vemerion.minecard.datagen;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

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
import mod.vemerion.minecard.game.ability.CardAbilityGroups;
import mod.vemerion.minecard.game.ability.CardAbilitySelection;
import mod.vemerion.minecard.game.ability.CardAbilityTrigger;
import mod.vemerion.minecard.game.ability.CardCondition;
import mod.vemerion.minecard.game.ability.CardPlacement;
import mod.vemerion.minecard.game.ability.CardSelectionMethod;
import mod.vemerion.minecard.game.ability.ChanceAbility;
import mod.vemerion.minecard.game.ability.CopyCardsAbility;
import mod.vemerion.minecard.game.ability.DrawCardsAbility;
import mod.vemerion.minecard.game.ability.ModifyAbility;
import mod.vemerion.minecard.game.ability.MultiAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import mod.vemerion.minecard.game.ability.ResourceAbility;
import mod.vemerion.minecard.game.ability.SummonCardAbility;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

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
		add(new Builder(EntityType.CREEPER, 1, 3, 2)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.DEATH, Optional.of(mod("origin_explosion")),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.ADJACENT)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-3, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.SHULKER, 3, 3, 3).addProperty(CardProperty.SHIELD, 1));
		add(new Builder(EntityType.DONKEY, 3, 2, 2).setCardAbility(new DrawCardsAbility(CardAbilityTrigger.SUMMON, 1)));
		add(new Builder(EntityType.ZOMBIE, 4, 4, 4)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.SELF)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(mod("shield"))),
								modification(0, new LazyCardType(mod("iron_equipment"))),
								modification(0, new LazyCardType(mod("diamond_sword")))))));
		add(new Builder(EntityType.STRAY, 2, 2, 2)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.TARGET)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.FREEZE, 1).build()))))));
		add(new Builder(EntityType.VINDICATOR, 6, 4, 8).setCardAbility(new AddCardsAbility(CardAbilityTrigger.DEATH,
				new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
						.setCardAbility(new ResourceAbility(CardAbilityTrigger.SUMMON, 1, 0))
						.setAdditionalData(new AdditionalCardData.ItemData(Items.EMERALD)).build()))));
		add(new Builder(EntityType.ENDERMAN, 6, 4, 5).setCardAbility(new CopyCardsAbility(CardAbilityTrigger.SUMMON,
				false, false, new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.ENEMY_HAND)),
						CardSelectionMethod.RANDOM, CardCondition.NoCondition.NO_CONDITION))));
		add(new Builder(EntityType.GLOW_SQUID, 3, 2, 2).setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON,
				Optional.of(new ResourceLocation(Main.MODID, "glow")),
				new CardAbilitySelection(
						new CardAbilityGroups(Set.of(CardAbilityGroup.YOUR_BOARD, CardAbilityGroup.ENEMY_BOARD)),
						CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
				List.of(modification(0, new LazyCardType(
						new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.STEALTH, 0).build()))))));
		add(new Builder(EntityType.WITHER_SKELETON, 5, 6, 4).setCardAbility(
				new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(
								Set.of(CardAbilityGroup.YOUR_HAND, CardAbilityGroup.YOUR_DECK)),
								CardSelectionMethod.ALL, new CardCondition.Entity(EntityType.WITHER)),
						List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, -2, 0, 0).build()))))));
		add(new Builder(EntityType.WITHER, 12, 10, 10)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.TICK, Optional.of(mod("wither_projectile")),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.RANDOM, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-4, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.SQUID, 1, 1, 1).setCardAbility(new CopyCardsAbility(CardAbilityTrigger.HURT, true,
				true, new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.SELF)),
						CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION))));
		add(new Builder(EntityType.SILVERFISH, 1, 1, 1).setCardAbility(new CopyCardsAbility(CardAbilityTrigger.HURT,
				true, false, new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.YOUR_DECK)),
						CardSelectionMethod.RANDOM, new CardCondition.Entity(EntityType.SILVERFISH)))));
		add(new Builder(EntityType.EVOKER, 5, 2, 2)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("evoker_fangs")),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-2, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.SHEEP, 2, 3, 2).setCardAbility(new SummonCardAbility(CardAbilityTrigger.DEATH,
				CardPlacement.ENEMY, new LazyCardType(new Builder(EntityType.ITEM, 0, 3, 0)
						.setAdditionalData(new AdditionalCardData.ItemData(Items.WHITE_WOOL)).build()))));
		add(new Builder(EntityType.VILLAGER, 6, 3, 3).setCardAbility(new SummonCardAbility(CardAbilityTrigger.HURT,
				CardPlacement.RIGHT, new LazyCardType(new Builder(EntityType.IRON_GOLEM, 0, 7, 7).build()))));
		add(new Builder(EntityType.ENDER_DRAGON, 10, 13, 5).setCardAbility(new MultiAbility(List.of(
				new SummonCardAbility(CardAbilityTrigger.SUMMON, CardPlacement.LEFT,
						new LazyCardType(mod("end_crystal"))),
				new SummonCardAbility(CardAbilityTrigger.SUMMON, CardPlacement.RIGHT,
						new LazyCardType(mod("end_crystal"))),
				new ModifyAbility(CardAbilityTrigger.TICK, Optional.of(mod("ender_dragon")),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-1, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))))));
		add(new Builder(EntityType.RABBIT, 1, 2, 1).setCardAbility(new ChanceAbility(30, new ModifyAbility(
				CardAbilityTrigger.SUMMON, Optional.empty(),
				new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.SELF)), CardSelectionMethod.ALL,
						CardCondition.NoCondition.NO_CONDITION),
				List.of(modification(0, new LazyCardType(
						new Builder(EntityType.ITEM, 0, 1, 1).addProperty(CardProperty.SPECIAL, 1).build())))))));
		add(new Builder(EntityType.POLAR_BEAR, 3, 4, 2).addProperty(CardProperty.BABY, 1)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.GROW, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.SELF)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 3, 1).addProperty(CardProperty.TAUNT, 1).build()))))));
		add(new Builder(EntityType.AXOLOTL, 2, 4, 1).setCardAbility(new ModifyAbility(CardAbilityTrigger.HURT,
				Optional.empty(),
				new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.SELF)), CardSelectionMethod.ALL,
						CardCondition.NoCondition.NO_CONDITION),
				List.of(modification(0, new LazyCardType(
						new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.STEALTH, 1).build()))))));
		add(new Builder(EntityType.BAT, 1, 2, 1)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.ALL, new CardCondition.Entity(EntityType.PLAYER)),
						List.of(modification(1, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(cod(50));
		add(new Builder(EntityType.HORSE, 4, 2, 4).addProperty(CardProperty.CHARGE, 1));
		add(new Builder(EntityType.MULE, 4, 4, 4).addProperty(CardProperty.BABY, 1)
				.setCardAbility(new DrawCardsAbility(CardAbilityTrigger.GROW, 2)));
		add(new Builder(EntityType.PIG, 2, 2, 3)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.DEATH, Optional.of(mod("throw_pork")),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.ALL, new CardCondition.Entity(EntityType.PLAYER)),
						List.of(modification(3, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.STRIDER, 4, 5, 4)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.ADJACENT)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.BURN, 0).build()))))));
		add(new Builder(EntityType.BLAZE, 6, 6, 5)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.TARGET)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.BURN, 3).build()))))));
		add(new Builder(EntityType.GHAST, 7, 3, 7)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("fireball")),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.ALL, new CardCondition.Entity(EntityType.PLAYER)),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.BURN, 4).build()))))));
		add(new Builder(EntityType.BEE, 5, 2, 2)
				.setCardAbility(new SummonCardAbility(CardAbilityTrigger.SUMMON, CardPlacement.RIGHT,
						new LazyCardType(new Builder(EntityType.ITEM, 0, 5, 0)
								.setAdditionalData(new AdditionalCardData.ItemData(Items.BEE_NEST))
								.setCardAbility(new SummonCardAbility(CardAbilityTrigger.TICK, CardPlacement.RIGHT,
										new LazyCardType(new Builder(EntityType.BEE, 0, 2, 2).build())))
								.build()))));
		add(new Builder(EntityType.IRON_GOLEM, 8, 7, 7).addProperty(CardProperty.TAUNT, 1));
		add(new Builder(EntityType.PANDA, 5, 4, 3).setCardAbility(new SummonCardAbility(CardAbilityTrigger.SUMMON,
				CardPlacement.RIGHT,
				new LazyCardType(new Builder(EntityType.ITEM, 0, 5, 0)
						.setAdditionalData(new AdditionalCardData.ItemData(Items.BAMBOO))
						.addProperty(CardProperty.BABY, 1)
						.setCardAbility(new ModifyAbility(CardAbilityTrigger.GROW, Optional.of(mod("bamboo")),
								new CardAbilitySelection(
										new CardAbilityGroups(
												Set.of(CardAbilityGroup.YOUR_BOARD, CardAbilityGroup.ENEMY_BOARD)),
										CardSelectionMethod.ALL, new CardCondition.Entity(EntityType.PANDA)),
								List.of(modification(0,
										new LazyCardType(new Builder(EntityType.ITEM, 0, 2, 2).build())))))
						.build()))));
		add(new Builder(EntityType.WOLF, 4, 5, 3).addProperty(CardProperty.TAUNT, 1));
		add(new Builder(EntityType.ZOMBIFIED_PIGLIN, 7, 9, 4)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.HURT, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.SELF)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 3).build()))))));
		add(new Builder(EntityType.HUSK, 3, 4, 2)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.TARGET)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, -2).build()))))));
		splitter(EntityType.SLIME, 7, NoCardAbility.NO_CARD_ABILITY);
		splitter(EntityType.MAGMA_CUBE, 9,
				new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.TARGET)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.BURN, 2).build())))));
		add(new Builder(EntityType.PHANTOM, 3, 2, 3).addProperty(CardProperty.STEALTH, 1));
		add(new Builder(EntityType.VEX, 2, 1, 2).addProperty(CardProperty.CHARGE, 1));

		// Auxiliary cards
		add(new Builder(EntityType.ITEM, 0, 1, 0).setKey(mod("shield")).addProperty(CardProperty.SHIELD, 1)
				.addEquipment(EquipmentSlot.OFFHAND, Items.SHIELD));
		add(new Builder(EntityType.ITEM, 0, 1, 1).setKey(mod("iron_equipment"))
				.addEquipment(EquipmentSlot.HEAD, Items.IRON_HELMET)
				.addEquipment(EquipmentSlot.MAINHAND, Items.IRON_SHOVEL));
		add(new Builder(EntityType.ITEM, 0, 0, 1).setKey(mod("diamond_sword")).addProperty(CardProperty.CHARGE, 1)
				.addEquipment(EquipmentSlot.MAINHAND, Items.DIAMOND_SWORD));
		add(new Builder(EntityType.ITEM, 0, 5, 0).setKey(mod("end_crystal"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.END_CRYSTAL))
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.TICK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.ADJACENT)),
								CardSelectionMethod.ALL, new CardCondition.Entity(EntityType.ENDER_DRAGON)),
						List.of(modification(4, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));

		printStatistics();

	}

	private void printStatistics() {
		Map<Integer, Integer> costCounts = new TreeMap<>();
		Map<CardProperty, Integer> propertyCounts = new TreeMap<>();
		for (var entry : cards.entrySet()) {
			var card = entry.getValue();
			if (Cards.isAllowed(card.getType()) && ForgeRegistries.ENTITIES.containsKey(entry.getKey())) {
				costCounts.compute(card.getCost(), (k, v) -> v == null ? 1 : v + 1);

				for (var property : card.getProperties().entrySet()) {
					if (property.getValue() > 0) {
						propertyCounts.compute(property.getKey(), (k, v) -> v == null ? 1 : v + 1);
					}
				}
			}
		}

		System.out.println("Card costs:");
		for (var entry : costCounts.entrySet())
			System.out.println(entry.getKey() + ": " + entry.getValue());
		System.out.println("Total cards: " + costCounts.values().stream().reduce(0, (a, b) -> a + b));
		System.out.println("Card properties:");
		for (var entry : propertyCounts.entrySet())
			System.out.println(entry.getKey().getName() + ": " + entry.getValue());
	}

	private void splitter(EntityType<?> entity, int cost, CardAbility ability) {
		var name = entity.getRegistryName().getPath();
		var small = mod("small_" + name);
		var medium = mod("medium_" + name);
		add(new Builder(entity, 0, 1, 1).setKey(small).setCardAbility(ability));
		add(new Builder(entity, 0, 2, 2).setKey(medium).setCardAbility(new MultiAbility(List.of(ability,
				new SummonCardAbility(CardAbilityTrigger.DEATH, CardPlacement.LEFT, new LazyCardType(small)),
				new SummonCardAbility(CardAbilityTrigger.DEATH, CardPlacement.RIGHT, new LazyCardType(small))))));
		add(new Builder(entity, cost, 4, 4).setCardAbility(new MultiAbility(List.of(ability,
				new SummonCardAbility(CardAbilityTrigger.DEATH, CardPlacement.LEFT, new LazyCardType(medium)),
				new SummonCardAbility(CardAbilityTrigger.DEATH, CardPlacement.RIGHT, new LazyCardType(medium))))));
	}

	private Builder cod(int chance) {
		if (chance == 0)
			return new Builder(EntityType.COD, 1, 1, 1);

		return new Builder(EntityType.COD, 1, 1, 1)
				.setCardAbility(new ChanceAbility(chance, new SummonCardAbility(CardAbilityTrigger.SUMMON,
						CardPlacement.RIGHT, new LazyCardType(cod(chance - 10).build()))));
	}

	private ModifyAbility.Modification modification(int healthChange, LazyCardType card) {
		return new ModifyAbility.Modification(healthChange, card);
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
