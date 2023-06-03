package mod.vemerion.minecard.datagen;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
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
import mod.vemerion.minecard.game.ability.ChoiceCardAbility;
import mod.vemerion.minecard.game.ability.CopyCardsAbility;
import mod.vemerion.minecard.game.ability.DrawCardsAbility;
import mod.vemerion.minecard.game.ability.GameOverAbility;
import mod.vemerion.minecard.game.ability.ModifyAbility;
import mod.vemerion.minecard.game.ability.MultiAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import mod.vemerion.minecard.game.ability.ResourceAbility;
import mod.vemerion.minecard.game.ability.SummonCardAbility;
import mod.vemerion.minecard.helper.Helper;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
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
		add(new Builder(EntityType.PLAYER, 0, 30, 0).setCardAbility(new GameOverAbility(CardAbilityTrigger.DEATH)));
		add(new Builder(EntityType.CREEPER, 1, 3, 2)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.DEATH, Optional.of(mod("origin_explosion")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ADJACENT)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-3, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.SHULKER, 3, 3, 3).addProperty(CardProperty.SHIELD, 1));
		add(new Builder(EntityType.DONKEY, 3, 2, 2).setCardAbility(new DrawCardsAbility(CardAbilityTrigger.SUMMON, 1)));
		add(new Builder(EntityType.ZOMBIE, 4, 4, 4)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(mod("shield"))),
								modification(0, new LazyCardType(mod("iron_equipment"))),
								modification(0, new LazyCardType(mod("diamond_sword")))))));
		add(new Builder(EntityType.STRAY, 2, 2, 2)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.TARGET)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.FREEZE, 2).build()))))));
		add(new Builder(EntityType.VINDICATOR, 6, 4, 8).setCardAbility(
				new AddCardsAbility(CardAbilityTrigger.DEATH, List.of(new LazyCardType(mod("emerald"))))));
		add(new Builder(EntityType.ENDERMAN, 6, 4, 5)
				.setCardAbility(new CopyCardsAbility(CardAbilityTrigger.SUMMON, false, false, false, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_HAND)),
								CardSelectionMethod.RANDOM, CardCondition.NoCondition.NO_CONDITION))));
		add(new Builder(EntityType.GLOW_SQUID, 3, 2, 2).setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON,
				Optional.of(new ResourceLocation(Main.MODID, "glow")),
				new CardAbilitySelection(
						new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD, CardAbilityGroup.ENEMY_BOARD)),
						CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
				List.of(modification(0, new LazyCardType(
						new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.STEALTH, 0).build()))))));
		add(new Builder(EntityType.WITHER_SKELETON, 5, 6, 4).setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON,
				Optional.empty(),
				new CardAbilitySelection(
						new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_HAND, CardAbilityGroup.YOUR_DECK)),
						CardSelectionMethod.ALL, new CardCondition.Entity(EntityType.WITHER)),
				List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, -2, 0, 0).build()))))));
		add(new Builder(EntityType.WITHER, 12, 10, 10)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.TICK, Optional.of(mod("wither_projectile")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.RANDOM, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-4, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.SQUID, 1, 1, 1)
				.setCardAbility(new CopyCardsAbility(CardAbilityTrigger.HURT, true, true, false, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION))));
		add(new Builder(EntityType.SILVERFISH, 1, 1, 1)
				.setCardAbility(new CopyCardsAbility(CardAbilityTrigger.HURT, true, false, false, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_DECK)),
								CardSelectionMethod.RANDOM, new CardCondition.Entity(EntityType.SILVERFISH)))));
		add(new Builder(EntityType.EVOKER, 5, 2, 2)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("evoker_fangs")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
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
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-1, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))))));
		add(new Builder(EntityType.RABBIT, 1, 2, 1).setCardAbility(new ChanceAbility(30, new ModifyAbility(
				CardAbilityTrigger.SUMMON, Optional.empty(),
				new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
						CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
				List.of(modification(0, new LazyCardType(
						new Builder(EntityType.ITEM, 0, 1, 1).addProperty(CardProperty.SPECIAL, 1).build())))))));
		add(new Builder(EntityType.POLAR_BEAR, 3, 4, 2).addProperty(CardProperty.BABY, 1)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.GROW, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 3, 1).addProperty(CardProperty.TAUNT, 1).build()))))));
		add(new Builder(EntityType.AXOLOTL, 2, 4, 1).setCardAbility(new ModifyAbility(CardAbilityTrigger.HURT,
				Optional.empty(),
				new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
						CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
				List.of(modification(0, new LazyCardType(
						new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.STEALTH, 1).build()))))));
		add(new Builder(EntityType.BAT, 1, 2, 1)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.ALL, new CardCondition.Entity(EntityType.PLAYER)),
						List.of(modification(1, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(cod(50));
		add(new Builder(EntityType.HORSE, 4, 2, 4).addProperty(CardProperty.CHARGE, 1));
		add(new Builder(EntityType.MULE, 4, 4, 4).addProperty(CardProperty.BABY, 1)
				.setCardAbility(new DrawCardsAbility(CardAbilityTrigger.GROW, 2)));
		add(new Builder(EntityType.PIG, 2, 2, 3)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.DEATH, Optional.of(mod("throw_pork")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.ALL, new CardCondition.Entity(EntityType.PLAYER)),
						List.of(modification(3, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.STRIDER, 4, 5, 4)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ADJACENT)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.BURN, 0).build()))))));
		add(new Builder(EntityType.BLAZE, 6, 6, 5)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.TARGET)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.BURN, 3).build()))))));
		add(new Builder(EntityType.GHAST, 7, 3, 7)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("fireball")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
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
												EnumSet.of(CardAbilityGroup.YOUR_BOARD, CardAbilityGroup.ENEMY_BOARD)),
										CardSelectionMethod.ALL, new CardCondition.Entity(EntityType.PANDA)),
								List.of(modification(0,
										new LazyCardType(new Builder(EntityType.ITEM, 0, 2, 2).build())))))
						.build()))));
		add(new Builder(EntityType.WOLF, 4, 5, 3).addProperty(CardProperty.TAUNT, 1));
		add(new Builder(EntityType.ZOMBIFIED_PIGLIN, 7, 9, 4)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.HURT, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 3).build()))))));
		add(new Builder(EntityType.HUSK, 3, 4, 2)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.TARGET)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, -2).build()))))));
		splitter(EntityType.SLIME, 7, NoCardAbility.NO_CARD_ABILITY);
		splitter(EntityType.MAGMA_CUBE, 9,
				new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.TARGET)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.BURN, 2).build())))));
		add(new Builder(EntityType.PHANTOM, 3, 2, 3).addProperty(CardProperty.STEALTH, 1));
		add(new Builder(EntityType.VEX, 2, 1, 2).addProperty(CardProperty.CHARGE, 1));
		add(new Builder(EntityType.PUFFERFISH, 1, 2, 1).addProperty(CardProperty.THORNS, 1));
		add(new Builder(EntityType.GUARDIAN, 5, 6, 4).addProperty(CardProperty.THORNS, 2));
		add(new Builder(EntityType.ELDER_GUARDIAN, 8, 9, 3).addProperty(CardProperty.THORNS, 3)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("elder_guardian")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, -1).build()))))));
		add(new Builder(EntityType.GOAT, 5, 4, 4).setCardAbility(
				new CopyCardsAbility(CardAbilityTrigger.SUMMON, true, false, true, Optional.of(mod("goat_charge")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.RANDOM,
								new CardCondition.Not(new CardCondition.Entity(EntityType.PLAYER))))));
		add(new Builder(EntityType.ENDERMITE, 3, 1, 2)
				.setCardAbility(new CopyCardsAbility(CardAbilityTrigger.SUMMON, true, false, false, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_DECK)),
								CardSelectionMethod.RANDOM, new CardCondition.Entity(EntityType.ENDERMAN)))));
		add(new Builder(EntityType.PIGLIN_BRUTE, 6, 5, 6).addProperty(CardProperty.TAUNT, 1));
		add(new Builder(EntityType.PILLAGER, 4, 2, 2)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_shield")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ADJACENT)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 1, 1).addProperty(CardProperty.TAUNT, 1).build()))))));
		add(new Builder(EntityType.RAVAGER, 9, 10, 4)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.ATTACK, Optional.of(mod("ravager_charge")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.TARGET_ADJACENT)),
								CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-4, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.SKELETON, 4, 4, 1)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.TICK, Optional.of(mod("shoot_arrow")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.RANDOM, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-1, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.SPIDER, 2, 2, 2)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.DEATH, Optional.of(mod("throw_web")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_HAND)),
								CardSelectionMethod.RANDOM, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 1, 0, 0).build()))))));
		add(new Builder(EntityType.TROPICAL_FISH, 0, 1, 1));
		add(new Builder(EntityType.SKELETON_HORSE, 4, 4, 4)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("lightning_bolt")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.RANDOM, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-2, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.BURN, 2).build()))))));
		add(new Builder(EntityType.SALMON, 1, 1, 1).setCardAbility(new ChanceAbility(50,
				new AddCardsAbility(CardAbilityTrigger.DEATH,
						List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
								.setAdditionalData(new AdditionalCardData.ItemData(Items.SALMON_BUCKET))
								.setCardAbility(new SummonCardAbility(CardAbilityTrigger.SUMMON, CardPlacement.RIGHT,
										new LazyCardType(EntityType.SALMON.getRegistryName())))
								.build()))))));
		add(new Builder(EntityType.OCELOT, 3, 3, 3).setCardAbility(new CopyCardsAbility(CardAbilityTrigger.SUMMON, true,
				false, true, Optional.empty(),
				new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
						CardSelectionMethod.ALL, new CardCondition.Or(new CardCondition.Entity(EntityType.CREEPER),
								new CardCondition.Entity(EntityType.PHANTOM))))));
		add(new Builder(EntityType.SNOW_GOLEM, 4, 4, 4)
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_snowball")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-2, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.FREEZE, 1).build()))))));
		add(new Builder(EntityType.CHICKEN, 2, 2, 1).setCardAbility(new ChanceAbility(50,
				new AddCardsAbility(CardAbilityTrigger.SUMMON,
						List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
								.setAdditionalData(new AdditionalCardData.ItemData(Items.EGG))
								.setCardAbility(
										new MultiAbility(List.of(
												new ModifyAbility(
														CardAbilityTrigger.SUMMON, Optional.of(
																mod("throw_egg")),
														new CardAbilitySelection(
																new CardAbilityGroups(
																		EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
																CardSelectionMethod.CHOICE,
																CardCondition.NoCondition.NO_CONDITION),
														List.of(modification(-1,
																new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
																		.build())))),
												new SummonCardAbility(CardAbilityTrigger.SUMMON, CardPlacement.RIGHT,
														new LazyCardType(EntityType.CHICKEN.getRegistryName())))))
								.build()))))));
		add(new Builder(EntityType.TURTLE, 5, 8, 1).addProperty(CardProperty.BABY, 1)
				.setCardAbility(new AddCardsAbility(CardAbilityTrigger.GROW,
						List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
								.setAdditionalData(new AdditionalCardData.ItemData(Items.SCUTE))
								.setCardAbility(
										new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_scute")),
												new CardAbilitySelection(
														new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD)),
														CardSelectionMethod.CHOICE,
														CardCondition.NoCondition.NO_CONDITION),
												List.of(modification(0,
														new LazyCardType(new Builder(EntityType.ITEM, 0, 5, 0)
																.addProperty(CardProperty.TAUNT, 1).build())))))
								.build())))));
		add(new Builder(EntityType.DROWNED, 7, 5, 9).setCardAbility(
				new AddCardsAbility(CardAbilityTrigger.DEATH, List.of(new LazyCardType(mod("trident"))))));
		add(new Builder(EntityType.COW, 2, 2, 2).setCardAbility(new AddCardsAbility(CardAbilityTrigger.SUMMON,
				List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
						.setAdditionalData(new AdditionalCardData.ItemData(Items.MILK_BUCKET))
						.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_milk")),
								new CardAbilitySelection(
										new CardAbilityGroups(
												EnumSet.of(CardAbilityGroup.YOUR_BOARD, CardAbilityGroup.ENEMY_BOARD)),
										CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
								List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
										.addProperty(CardProperty.BURN, 0).addProperty(CardProperty.FREEZE, 0)
										.addProperty(CardProperty.SHIELD, 0).addProperty(CardProperty.STEALTH, 0)
										.addProperty(CardProperty.TAUNT, 0).addProperty(CardProperty.THORNS, 0)
										.addProperty(CardProperty.POISON, 0).build())))))
						.build())))));
		add(new Builder(EntityType.FOX, 4, 3, 2).setCardAbility(new SummonCardAbility(CardAbilityTrigger.SUMMON,
				CardPlacement.RIGHT, new LazyCardType(mod("sweet_berries")))));
		add(new Builder(EntityType.MOOSHROOM, 2, 2, 2).setCardAbility(new AddCardsAbility(CardAbilityTrigger.SUMMON,
				List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
						.setAdditionalData(
								new AdditionalCardData.ItemData(Items.MUSHROOM_STEW))
						.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON,
								Optional.of(mod("throw_mushroom_stew")),
								new CardAbilitySelection(
										new CardAbilityGroups(
												EnumSet.of(CardAbilityGroup.YOUR_BOARD, CardAbilityGroup.ENEMY_BOARD)),
										CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
								List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
										.addProperty(CardProperty.BABY, 0).build())))))
						.build())))));
		add(new Builder(EntityType.PARROT, 1, 2, 1).addProperty(CardProperty.TAUNT, 1));
		add(new Builder(EntityType.LLAMA, 1, 1, 1).setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON,
				Optional.of(mod("llama_spit")),
				new CardAbilitySelection(
						new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD, CardAbilityGroup.ENEMY_BOARD)),
						CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
				List.of(modification(-1, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.CAT, 3, 3, 1)
				.setCardAbility(new ChanceAbility(50, new AddCardsAbility(CardAbilityTrigger.TICK,
						List.of(new LazyCardType(mod("rotten_flesh")), new LazyCardType(mod("rabbit_foot")))))));
		add(new Builder(EntityType.CAVE_SPIDER, 3, 2, 2).setCardAbility(new ChanceAbility(50, new ModifyAbility(
				CardAbilityTrigger.ATTACK, Optional.empty(),
				new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.TARGET)),
						CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
				List.of(modification(0, new LazyCardType(
						new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.POISON, 1).build())))))));
		add(new Builder(EntityType.WITCH, 6, 5, 4).setCardAbility(
				new AddCardsAbility(CardAbilityTrigger.SUMMON, List.of(new LazyCardType(mod("absorption_potion")),
						new LazyCardType(mod("poison_potion")), new LazyCardType(mod("healing_potion"))))));
		add(new Builder(EntityType.WANDERING_TRADER, 7, 7, 4).setCardAbility(new ChoiceCardAbility(List.of(
				new AddCardsAbility(CardAbilityTrigger.SUMMON, List.of(new LazyCardType(mod("pufferfish_bucket")))),
				new AddCardsAbility(CardAbilityTrigger.SUMMON, List.of(new LazyCardType(mod("packed_ice")))),
				new AddCardsAbility(CardAbilityTrigger.SUMMON, List.of(new LazyCardType(mod("pointed_dripstone"))))))));
		add(new Builder(EntityType.TRADER_LLAMA, 5, 4, 4)
				.setCardAbility(
						new ChoiceCardAbility(List.of(
								new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("llama_spit")),
										new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD,
														CardAbilityGroup.ENEMY_BOARD)),
												CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
										List.of(modification(-2,
												new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build())))),
								new DrawCardsAbility(CardAbilityTrigger.SUMMON, 1)))));
		add(new Builder(EntityType.DOLPHIN, 3, 2, 3).setCardAbility(new SummonCardAbility(CardAbilityTrigger.SUMMON,
				CardPlacement.RIGHT, new LazyCardType(mod("buried_treasure")))));
		add(new Builder(EntityType.ZOMBIE_VILLAGER, 6, 3, 3)
				.setCardAbility(new SummonCardAbility(CardAbilityTrigger.DEATH, CardPlacement.RIGHT,
						new LazyCardType(EntityType.VILLAGER.getRegistryName()))));
		add(new Builder(EntityType.HOGLIN, 8, 5, 7)
				.setCardAbility(new ChoiceCardAbility(List.of(
						new SummonCardAbility(CardAbilityTrigger.SUMMON, CardPlacement.RIGHT,
								new LazyCardType(new Builder(EntityType.HOGLIN, 0, 3, 4)
										.addProperty(CardProperty.BABY, 1).build())),
						new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.empty(),
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
								List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 1, 1)
										.addProperty(CardProperty.TAUNT, 1).build()))))))));
		add(new Builder(EntityType.ZOGLIN, 9, 6, 12).setCardAbility(
				new AddCardsAbility(CardAbilityTrigger.DEATH, List.of(new LazyCardType(mod("rotten_flesh"))))));
		add(new Builder(EntityType.PIGLIN, 5, 4, 4).setCardAbility(
				new AddCardsAbility(CardAbilityTrigger.SUMMON, List.of(new LazyCardType(mod("iron_boots")),
						new LazyCardType(mod("ender_pearl")), new LazyCardType(mod("fire_charge"))))));

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
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ADJACENT)),
								CardSelectionMethod.ALL, new CardCondition.Entity(EntityType.ENDER_DRAGON)),
						List.of(modification(4, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("trident"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.TRIDENT))
				.setCardAbility(new MultiAbility(List.of(
						new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_trident")),
								new CardAbilitySelection(
										new CardAbilityGroups(
												EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
										CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
								List.of(modification(-4,
										new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
												.addProperty(CardProperty.BURN, 4).build())))),
						new ChanceAbility(30, new AddCardsAbility(CardAbilityTrigger.SUMMON,
								List.of(new LazyCardType(mod("trident")))))))));
		add(new Builder(EntityType.ITEM, 0, 3, 0).setKey(mod("sweet_berries"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.SWEET_BERRIES))
				.addProperty(CardProperty.THORNS, 2)
				.setCardAbility(new AddCardsAbility(CardAbilityTrigger.DEATH,
						List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
								.setAdditionalData(new AdditionalCardData.ItemData(Items.SWEET_BERRIES))
								.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON,
										Optional.of(mod("throw_sweet_berries")),
										new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
										List.of(modification(3,
												new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build())))))
								.build())))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("rotten_flesh"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.ROTTEN_FLESH))
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_rotten_flesh")),
						new CardAbilitySelection(
								new CardAbilityGroups(
										EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(-2, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build())),
								modification(2, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("rabbit_foot"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.RABBIT_FOOT))
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_rabbit_foot")),
						new CardAbilitySelection(
								new CardAbilityGroups(
										EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 1, 0).build()))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("absorption_potion"))
				.setAdditionalData(new AdditionalCardData.ItemData(
						PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.TURTLE_MASTER)))
				.setCardAbility(
						new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_absorption_potion")),
								new CardAbilitySelection(
										new CardAbilityGroups(
												EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
										CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
								List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
										.addProperty(CardProperty.SHIELD, 1).build()))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("poison_potion"))
				.setAdditionalData(new AdditionalCardData.ItemData(
						PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.POISON)))
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_poison_potion")),
						new CardAbilitySelection(
								new CardAbilityGroups(
										EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.POISON, 1).build()))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("healing_potion"))
				.setAdditionalData(new AdditionalCardData.ItemData(
						PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.HEALING)))
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_healing_potion")),
						new CardAbilitySelection(
								new CardAbilityGroups(
										EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(5, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0).build()))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("pufferfish_bucket"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.PUFFERFISH_BUCKET))
				.setCardAbility(new SummonCardAbility(CardAbilityTrigger.SUMMON, CardPlacement.RIGHT,
						new LazyCardType(EntityType.PUFFERFISH.getRegistryName()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("packed_ice"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.PACKED_ICE))
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_packed_ice")),
						new CardAbilitySelection(
								new CardAbilityGroups(
										EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.FREEZE, 2).build()))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("pointed_dripstone"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.POINTED_DRIPSTONE))
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON,
						Optional.of(mod("throw_pointed_dripstone")),
						new CardAbilitySelection(
								new CardAbilityGroups(
										EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.THORNS, 3).build()))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("iron_sword"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.IRON_SWORD))
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_iron_sword")),
						new CardAbilitySelection(
								new CardAbilityGroups(
										EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 2)
								.addEquipment(EquipmentSlot.MAINHAND, Items.IRON_SWORD).build()))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("emerald"))
				.setCardAbility(new ResourceAbility(CardAbilityTrigger.SUMMON, 1, 0))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.EMERALD)));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("leather_chestplate"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.LEATHER_CHESTPLATE)).setCardAbility(
						new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_leather_chestplate")),
								new CardAbilitySelection(
										new CardAbilityGroups(
												EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
										CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
								List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 2, 0)
										.addEquipment(EquipmentSlot.MAINHAND, Items.LEATHER_CHESTPLATE).build()))))));
		add(new Builder(EntityType.ITEM, 0, 2, 0).setKey(mod("buried_treasure"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.CHEST.getDefaultInstance()
						.setHoverName(new TranslatableComponent(Helper.gui("buried_treasure")))))
				.setCardAbility(new AddCardsAbility(CardAbilityTrigger.DEATH, List.of(new LazyCardType(mod("emerald")),
						new LazyCardType(mod("leather_chestplate")), new LazyCardType(mod("iron_sword"))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("iron_boots"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.IRON_BOOTS))
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("throw_iron_boots")),
						new CardAbilitySelection(
								new CardAbilityGroups(
										EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(new Builder(EntityType.ITEM, 0, 2, 0)
								.addEquipment(EquipmentSlot.FEET, Items.IRON_BOOTS).build()))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("fire_charge"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.FIRE_CHARGE))
				.setCardAbility(new ModifyAbility(CardAbilityTrigger.SUMMON, Optional.of(mod("fireball")),
						new CardAbilitySelection(
								new CardAbilityGroups(
										EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.CHOICE, CardCondition.NoCondition.NO_CONDITION),
						List.of(modification(0, new LazyCardType(
								new Builder(EntityType.ITEM, 0, 0, 0).addProperty(CardProperty.BURN, 3).build()))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("ender_pearl"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.ENDER_PEARL))
				.setCardAbility(new CopyCardsAbility(CardAbilityTrigger.SUMMON, true, true, false,
						Optional.of(mod("throw_ender_pearl")),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD)),
								CardSelectionMethod.CHOICE,
								new CardCondition.Not(new CardCondition.Entity(EntityType.PLAYER))))));

		printStatistics();
	}

	private void printStatistics() {
		Map<Integer, Integer> costCounts = new TreeMap<>();
		Map<CardProperty, Integer> propertyCounts = new TreeMap<>();
		Set<EntityType<?>> types = new HashSet<>();
		for (var entry : cards.entrySet()) {
			var card = entry.getValue();
			types.add(card.getType());
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

		for (var entry : ForgeRegistries.ENTITIES.getEntries()) {
			if (Cards.isAllowed(entry.getValue()) && !types.contains(entry.getValue())) {
				System.out.println("Missing " + entry.getKey());
			}
		}
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
