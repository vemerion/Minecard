package mod.vemerion.minecard.datagen;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.AdditionalCardData;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.CardType;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.LazyCardType;
import mod.vemerion.minecard.game.ability.AddAbilityAbility;
import mod.vemerion.minecard.game.ability.AnimationAbility;
import mod.vemerion.minecard.game.ability.CardAbility;
import mod.vemerion.minecard.game.ability.CardAbilityGroup;
import mod.vemerion.minecard.game.ability.CardAbilityGroups;
import mod.vemerion.minecard.game.ability.CardAbilitySelection;
import mod.vemerion.minecard.game.ability.CardAbilityTrigger;
import mod.vemerion.minecard.game.ability.CardCondition;
import mod.vemerion.minecard.game.ability.CardModification;
import mod.vemerion.minecard.game.ability.CardOperator;
import mod.vemerion.minecard.game.ability.CardPlacement;
import mod.vemerion.minecard.game.ability.CardSelectionMethod;
import mod.vemerion.minecard.game.ability.CardVariable;
import mod.vemerion.minecard.game.ability.ChainAbility;
import mod.vemerion.minecard.game.ability.ChanceAbility;
import mod.vemerion.minecard.game.ability.ChoiceCardAbility;
import mod.vemerion.minecard.game.ability.ConstantCardsAbility;
import mod.vemerion.minecard.game.ability.DrawCardsAbility;
import mod.vemerion.minecard.game.ability.GameOverAbility;
import mod.vemerion.minecard.game.ability.HistoryAbility;
import mod.vemerion.minecard.game.ability.ModifyAbility;
import mod.vemerion.minecard.game.ability.MoveCollectedAbility;
import mod.vemerion.minecard.game.ability.MultiAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import mod.vemerion.minecard.game.ability.PlaceCardsAbility;
import mod.vemerion.minecard.game.ability.RemoveCardsAbility;
import mod.vemerion.minecard.game.ability.ResourceAbility;
import mod.vemerion.minecard.game.ability.SelectCardsAbility;
import mod.vemerion.minecard.game.ability.TriggerAdvancementAbility;
import mod.vemerion.minecard.helper.Helper;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.registries.ForgeRegistries;

public class ModCardProvider implements DataProvider {
	private Map<ResourceLocation, CardType> cards = new HashMap<>();
	private PackOutput packOutput;

	public ModCardProvider(PackOutput packOutput) {
		this.packOutput = packOutput;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		addCards();
		var folder = packOutput.getOutputFolder();
		var list = new ArrayList<CompletableFuture<?>>();
		for (var entry : cards.entrySet()) {
			var key = entry.getKey();
			var path = folder
					.resolve("data/" + key.getNamespace() + "/" + Cards.FOLDER_NAME + "/" + key.getPath() + ".json");
			list.add(DataProvider.saveStable(cache,
					CardType.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, s -> {
					}), path));
		}
		return CompletableFuture.allOf(list.toArray((length) -> {
			return new CompletableFuture[length];
		}));
	}

	// Give player card:
	// /give <player> minecard:card{minecard_carddata:{type:'<resource location>'}}

	public static String textKey(String s) {
		return "card_ability." + Main.MODID + "." + s;
	}

	public CardAbility history() {
		return new HistoryAbility(Items.BOOK);
	}

	private void addCards() {
		// Entity card
		add(new Builder(EntityType.PLAYER, 0, 30, 0)
				.setCardAbility(new GameOverAbility(EnumSet.of(CardAbilityTrigger.DEATH), textKey("player"))));
		add(new Builder(EntityType.CREEPER, 1, 3, 2)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.DEATH), textKey("creeper"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ADJACENT)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new AnimationAbility(mod("origin_explosion")),
						new ModifyAbility(List.of(new ModificationBuilder().heal(-3).build())), history()))));
		add(new Builder(EntityType.SHULKER, 3, 3, 3).addProperty(CardProperty.SHIELD, 1));
		add(new Builder(EntityType.DONKEY, 3, 2, 2)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("donkey"),
						List.of(new DrawCardsAbility(Set.of(), "", 1), history()))));
		add(new Builder(EntityType.ZOMBIE, 4, 4, 4).addProperty(CardProperty.UNDEAD, 1).setCardAbility(new ChainAbility(
				EnumSet.of(CardAbilityTrigger.SUMMON), textKey("zombie"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(
										EnumSet.of(CardAbilityGroup.SELF)), CardSelectionMethod.All.ALL,
										CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(List.of(
								new ModificationBuilder().addMaxHealth(1).setProperty(CardProperty.SHIELD, 1).build(),
								new ModificationBuilder().addMaxHealth(1).addDamage(1).build(),
								new ModificationBuilder().addDamage(1).setProperty(CardProperty.CHARGE, 1).build())),
						history()))));
		add(new Builder(EntityType.STRAY, 2, 2, 2).addProperty(CardProperty.UNDEAD, 1)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.ATTACK), textKey("stray"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.TARGET)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(
								List.of(new ModificationBuilder().addProperty(CardProperty.FREEZE, 2).build()))))));
		add(new Builder(EntityType.VINDICATOR, 6, 4, 8).setCardAbility(addCards(EnumSet.of(CardAbilityTrigger.DEATH),
				textKey("vindicator"), List.of(new LazyCardType(mod("emerald"))))));
		add(new Builder(EntityType.ENDERMAN, 6, 4, 5)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("enderman"),
						List.of(new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_HAND)),
								new CardSelectionMethod.Random(1, false), CardCondition.NoCondition.NO_CONDITION)),
								new PlaceCardsAbility(CardPlacement.YOUR_HAND), new HistoryAbility(
										Items.BOOK.getDefaultInstance(), Optional.of(HistoryEntry.Visibility.ALL))))));
		add(new Builder(EntityType.GLOW_SQUID, 3, 2, 2)
				.setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("glow_squid"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD,
														CardAbilityGroup.ENEMY_BOARD)),
												CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("glow")),
										new ModifyAbility(List.of(new ModificationBuilder()
												.setProperty(CardProperty.STEALTH, 0).build())),
										history()))));
		add(new Builder(EntityType.WITHER_SKELETON, 5, 6, 4).addProperty(CardProperty.UNDEAD, 1)
				.setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("wither_skeleton"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_HAND,
														CardAbilityGroup.YOUR_DECK)),
												CardSelectionMethod.All.ALL,
												new CardCondition.Entity(EntityType.WITHER))),
										new ModifyAbility(List.of(new ModificationBuilder().addCost(-2).build()))))));
		add(new Builder(EntityType.WITHER, 12, 10, 10).addProperty(CardProperty.UNDEAD, 1).setDropChance(1)
				.setDeckCount(
						1)
				.setCardAbility(
						new MultiAbility(
								textKey("wither"), List.of(
										new ChainAbility(
												EnumSet.of(CardAbilityTrigger.TICK), "", List.of(
														new SelectCardsAbility(new CardAbilitySelection(
																new CardAbilityGroups(
																		EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
																new CardSelectionMethod.Random(1, false),
																CardCondition.NoCondition.NO_CONDITION)),
														new AnimationAbility(mod("wither_projectile")),
														new ModifyAbility(
																List.of(new ModificationBuilder().heal(-4).build())),
														history())),
										new AnimationAbility(Set.of(CardAbilityTrigger.SUMMON), mod("wither")),
										new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), "",
												List.of(new TriggerAdvancementAbility(
														new ResourceLocation(Main.MODID, "discount_wither"),
														new CardCondition.Not(new CardCondition.OperatorCondition(
																new CardOperator.GreaterThan(
																		new CardOperator.Variable(CardVariable.COST),
																		new CardOperator.Constant(6)))))))))));
		add(new Builder(EntityType.SQUID, 1, 1, 1).setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.HURT),
				textKey("squid"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new RemoveCardsAbility(),
						new ModifyAbility(List.of(new ModificationBuilder().restore().build())),
						new PlaceCardsAbility(CardPlacement.YOUR_HAND)))));
		add(new Builder(EntityType.SILVERFISH, 1, 1, 1).setCardAbility(new ChainAbility(
				EnumSet.of(CardAbilityTrigger.HURT), textKey("silverfish"),
				List.of(new SelectCardsAbility(new CardAbilitySelection(
						new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_DECK)),
						new CardSelectionMethod.Random(1, false), new CardCondition.Entity(EntityType.SILVERFISH))),
						new RemoveCardsAbility(), new PlaceCardsAbility(CardPlacement.YOUR_HAND), history()))));
		add(new Builder(EntityType.EVOKER, 5, 2, 2)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("evoker"),
						List.of(new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
								new AnimationAbility(mod("evoker_fangs")),
								new ModifyAbility(List.of(new ModificationBuilder().heal(-2).build())), history()))));
		add(new Builder(EntityType.SHEEP, 2, 3, 2).setCardAbility(summon(EnumSet.of(CardAbilityTrigger.DEATH),
				textKey("sheep"), CardPlacement.ENEMY, new LazyCardType(new Builder(EntityType.ITEM, 0, 3, 0)
						.setAdditionalData(new AdditionalCardData.ItemData(Items.WHITE_WOOL)).build()))));
		add(new Builder(EntityType.VILLAGER, 6, 3, 3).setCardAbility(new ChainAbility(
				EnumSet.of(CardAbilityTrigger.HURT), textKey("villager"),
				List.of(new ConstantCardsAbility(
						List.of(new LazyCardType(new Builder(EntityType.IRON_GOLEM, 0, 7, 7).build()))),
						new PlaceCardsAbility(CardPlacement.RIGHT), history(),
						new MoveCollectedAbility(0, 1, true, false),
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(List.of(new ModificationBuilder()
								.addProperty(CardProperty.ADVANCEMENT_COUNTER, new CardOperator.CollectedCount(1))
								.build())),
						new TriggerAdvancementAbility(new ResourceLocation(Main.MODID, "iron_golem_farm"),
								new CardCondition.OperatorCondition(new CardOperator.GreaterThan(
										new CardOperator.Variable(
												new CardVariable.PropertyVariable(CardProperty.ADVANCEMENT_COUNTER)),
										new CardOperator.Constant(2))))))));
		add(new Builder(EntityType.ENDER_DRAGON, 10, 13, 5)
				.setDeckCount(1).setDropChance(
						1)
				.setCardAbility(
						new MultiAbility(textKey("ender_dragon"),
								List.of(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), "",
										List.of(new ConstantCardsAbility(List.of(new LazyCardType(mod("end_crystal")))),
												new PlaceCardsAbility(CardPlacement.LEFT),
												new PlaceCardsAbility(CardPlacement.RIGHT),
												new ConstantCardsAbility(List.of(new LazyCardType(mod("end_crystal")))),
												history())),
										new ChainAbility(
												EnumSet.of(CardAbilityTrigger.TICK), "", List.of(
														new SelectCardsAbility(new CardAbilitySelection(
																new CardAbilityGroups(
																		EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
																CardSelectionMethod.All.ALL,
																CardCondition.NoCondition.NO_CONDITION)),
														new AnimationAbility(mod("ender_dragon")),
														new ModifyAbility(
																List.of(new ModificationBuilder().heal(-1).build())),
														history()))))));
		add(new Builder(EntityType.RABBIT, 1, 2, 1).setCardAbility(new ChanceAbility(textKey("rabbit"), 30,
				new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), "", List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(List.of(new ModificationBuilder().addMaxHealth(1).addDamage(1)
								.setProperty(CardProperty.SPECIAL, 1).build())),
						history())))));
		add(new Builder(EntityType.POLAR_BEAR, 3, 4, 2).addProperty(CardProperty.BABY, 1)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.GROW), textKey("polar_bear"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(List.of(new ModificationBuilder().addMaxHealth(3).addDamage(1)
								.setProperty(CardProperty.TAUNT, 1).build())),
						history()))));
		add(new Builder(EntityType.AXOLOTL, 2, 4, 1)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.HURT), textKey("axolotl"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(
								List.of(new ModificationBuilder().setProperty(CardProperty.STEALTH, 1).build()))))));
		add(new Builder(EntityType.BAT, 1, 2, 1).setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.ATTACK),
				textKey("bat"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD)),
										CardSelectionMethod.All.ALL, new CardCondition.Entity(EntityType.PLAYER))),
						new ModifyAbility(List.of(new ModificationBuilder().heal(1).build())), history()))));
		add(cod(50));
		add(new Builder(EntityType.HORSE, 4, 2, 4).addProperty(CardProperty.CHARGE, 1));
		add(new Builder(EntityType.MULE, 4, 4, 4).addProperty(CardProperty.BABY, 1)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.GROW), textKey("mule"),
						List.of(new DrawCardsAbility(Set.of(), "", 2), history()))));
		add(new Builder(EntityType.PIG, 2, 2, 3).setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.DEATH),
				textKey("pig"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD)),
										CardSelectionMethod.All.ALL, new CardCondition.Entity(EntityType.PLAYER))),
						new AnimationAbility(mod("throw_pork")),
						new ModifyAbility(List.of(new ModificationBuilder().heal(3).build())), history()))));
		add(new Builder(EntityType.STRIDER, 4, 5, 4)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("strider"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ADJACENT)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(List.of(new ModificationBuilder().setProperty(CardProperty.BURN, 0).build())),
						history()))));
		add(new Builder(EntityType.BLAZE, 6, 6, 5)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.ATTACK), textKey("blaze"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.TARGET)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(
								List.of(new ModificationBuilder().addProperty(CardProperty.BURN, 3).build()))))));
		add(new Builder(EntityType.GHAST, 7, 3, 7)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("ghast"),
						List.of(new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.All.ALL, new CardCondition.Entity(EntityType.PLAYER))),
								new AnimationAbility(mod("fireball")),
								new ModifyAbility(
										List.of(new ModificationBuilder().addProperty(CardProperty.BURN, 4).build())),
								history()))));
		add(new Builder(EntityType.BEE, 5, 2, 2).setCardAbility(summon(EnumSet.of(CardAbilityTrigger.SUMMON),
				textKey("bee"), CardPlacement.RIGHT,
				new LazyCardType(new Builder(EntityType.ITEM, 0, 5, 0)
						.setAdditionalData(new AdditionalCardData.ItemData(Items.BEE_NEST))
						.setCardAbility(summon(EnumSet.of(CardAbilityTrigger.TICK), textKey("bee_nest"),
								CardPlacement.RIGHT, new LazyCardType(new Builder(EntityType.BEE, 0, 2, 2).build())))
						.build()))));
		add(new Builder(EntityType.IRON_GOLEM, 8, 7, 7).addProperty(CardProperty.TAUNT, 1));
		add(new Builder(EntityType.PANDA, 5, 4, 3).setCardAbility(summon(EnumSet.of(CardAbilityTrigger.SUMMON),
				textKey("panda"), CardPlacement.RIGHT,
				new LazyCardType(new Builder(EntityType.ITEM, 0, 5, 0)
						.setAdditionalData(new AdditionalCardData.ItemData(Items.BAMBOO))
						.addProperty(CardProperty.BABY, 1)
						.setCardAbility(
								new ChainAbility(EnumSet.of(CardAbilityTrigger.GROW), textKey("bamboo"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD,
														CardAbilityGroup.ENEMY_BOARD)),
												CardSelectionMethod.All.ALL,
												new CardCondition.Entity(EntityType.PANDA))),
										new AnimationAbility(mod("throw_bamboo")),
										new ModifyAbility(List
												.of(new ModificationBuilder().addMaxHealth(2).addDamage(2).build())),
										history())))
						.build()))));
		add(new Builder(EntityType.WOLF, 4, 5, 3).addProperty(CardProperty.TAUNT, 1));
		add(new Builder(EntityType.ZOMBIFIED_PIGLIN, 7, 9, 4).addProperty(CardProperty.UNDEAD, 1).setCardAbility(
				new ChainAbility(EnumSet.of(CardAbilityTrigger.HURT), textKey("zombified_piglin"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(List.of(new ModificationBuilder().addDamage(3).build())), history()))));
		add(new Builder(EntityType.HUSK, 3, 4, 2).addProperty(CardProperty.UNDEAD, 1)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.ATTACK), textKey("husk"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.TARGET)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(List.of(new ModificationBuilder().addDamage(-2).build()))))));
		splitter(EntityType.SLIME, 7, NoCardAbility.NO_CARD_ABILITY);
		splitter(EntityType.MAGMA_CUBE, 9,
				new ChainAbility(EnumSet.of(CardAbilityTrigger.ATTACK), textKey("magma_cube"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.TARGET)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(
								List.of(new ModificationBuilder().addProperty(CardProperty.BURN, 2).build())))));
		add(new Builder(EntityType.PHANTOM, 3, 2, 3).addProperty(CardProperty.UNDEAD, 1)
				.addProperty(CardProperty.STEALTH, 1));
		add(new Builder(EntityType.VEX, 2, 1, 2).addProperty(CardProperty.CHARGE, 1));
		add(new Builder(EntityType.PUFFERFISH, 1, 2, 1).addProperty(CardProperty.THORNS, 1));
		add(new Builder(EntityType.GUARDIAN, 5, 6, 4).addProperty(CardProperty.THORNS, 2));
		add(new Builder(EntityType.ELDER_GUARDIAN, 8, 9, 3).addProperty(CardProperty.THORNS, 3)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("elder_guardian"),
						List.of(new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
								new AnimationAbility(mod("elder_guardian")),
								new ModifyAbility(List.of(new ModificationBuilder().addDamage(-1).build())),
								history()))));
		add(new Builder(EntityType.GOAT, 5, 4, 4)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("goat"),
						List.of(new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								new CardSelectionMethod.Random(1, false),
								new CardCondition.Not(new CardCondition.Entity(EntityType.PLAYER)))),
								new AnimationAbility(mod("goat_charge")), new RemoveCardsAbility(),
								new PlaceCardsAbility(CardPlacement.ENEMY_HAND), new HistoryAbility(
										Items.BOOK.getDefaultInstance(), Optional.of(HistoryEntry.Visibility.ALL))))));
		add(new Builder(EntityType.ENDERMITE, 3, 1, 2).setCardAbility(new ChainAbility(
				EnumSet.of(CardAbilityTrigger.SUMMON), textKey("endermite"),
				List.of(new SelectCardsAbility(new CardAbilitySelection(
						new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_DECK)),
						new CardSelectionMethod.Random(1, false), new CardCondition.Entity(EntityType.ENDERMAN))),
						new RemoveCardsAbility(), new PlaceCardsAbility(CardPlacement.YOUR_HAND), history()))));
		add(new Builder(EntityType.PIGLIN_BRUTE, 6, 5, 6).addProperty(CardProperty.TAUNT, 1));
		add(new Builder(EntityType.PILLAGER, 4, 2, 2)
				.setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("pillager"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ADJACENT)),
												CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_shield")),
										new ModifyAbility(List.of(new ModificationBuilder().addMaxHealth(1).addDamage(1)
												.setProperty(CardProperty.TAUNT, 1).build())),
										history()))));
		add(new Builder(EntityType.RAVAGER, 9, 10, 4)
				.setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.ATTACK), textKey("ravager"), List.of(
										new SelectCardsAbility(
												new CardAbilitySelection(new CardAbilityGroups(
														EnumSet.of(CardAbilityGroup.SELF)), CardSelectionMethod.All.ALL,
														CardCondition.NoCondition.NO_CONDITION)),
										new MoveCollectedAbility(0, 1, true, false),
										new SelectCardsAbility(
												new CardAbilitySelection(
														new CardAbilityGroups(
																EnumSet.of(CardAbilityGroup.TARGET_ADJACENT)),
														CardSelectionMethod.All.ALL,
														CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod(
												"ravager_charge")),
										new ModifyAbility(List.of(new ModificationBuilder()
												.put(new CardModification(CardVariable.HEALTH,
														new CardOperator.Negate(new CardOperator.CollectedAny(1,
																new CardOperator.Variable(CardVariable.DAMAGE)))))
												.build())),
										history(),
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.COLLECTED)),
												CardSelectionMethod.All.ALL,
												new CardCondition.Not(new CardCondition.OperatorCondition(
														new CardOperator.GreaterThan(
																new CardOperator.Variable(CardVariable.HEALTH),
																new CardOperator.Constant(0))))),
												true),
										new TriggerAdvancementAbility(new ResourceLocation(Main.MODID, "sweeping_edge"),
												new CardCondition.OperatorCondition(
														new CardOperator.GreaterThan(new CardOperator.CollectedCount(0),
																new CardOperator.Constant(1))))))));
		add(new Builder(EntityType.SKELETON, 4, 4, 1).addProperty(CardProperty.UNDEAD, 1)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.TICK), textKey("skeleton"),
						List.of(new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								new CardSelectionMethod.Random(1, false), CardCondition.NoCondition.NO_CONDITION)),
								new AnimationAbility(mod("shoot_arrow")),
								new ModifyAbility(List.of(new ModificationBuilder().heal(-1).build())), history()))));
		add(new Builder(EntityType.SPIDER, 2, 2, 2)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.DEATH), textKey("spider"),
						List.of(new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_HAND)),
								new CardSelectionMethod.Random(1, false), CardCondition.NoCondition.NO_CONDITION)),
								new AnimationAbility(mod("throw_web")),
								new ModifyAbility(List.of(new ModificationBuilder().addCost(1).build())), history()))));
		add(new Builder(EntityType.TROPICAL_FISH, 0, 1, 1));
		add(new Builder(EntityType.SKELETON_HORSE, 4, 4, 4).addProperty(CardProperty.UNDEAD, 1)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("skeleton_horse"),
						List.of(new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								new CardSelectionMethod.Random(1, false), CardCondition.NoCondition.NO_CONDITION)),
								new AnimationAbility(mod("lightning_bolt")),
								new ModifyAbility(List.of(
										new ModificationBuilder().heal(-2).addProperty(CardProperty.BURN, 2).build())),
								history()))));
		add(new Builder(EntityType.SALMON, 1, 1, 1)
				.setCardAbility(
						new ChanceAbility(textKey("salmon"), 50,
								addCards(EnumSet.of(CardAbilityTrigger.DEATH), "",
										List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
												.setAdditionalData(new AdditionalCardData.ItemData(
														Items.SALMON_BUCKET))
												.setCardAbility(summon(EnumSet.of(CardAbilityTrigger.SUMMON),
														textKey("salmon_bucket"), CardPlacement.RIGHT,
														new LazyCardType(ForgeRegistries.ENTITY_TYPES
																.getKey(EntityType.SALMON))))
												.build()))))));
		add(new Builder(EntityType.OCELOT, 3, 3, 3)
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("ocelot"), List.of(
						new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								CardSelectionMethod.All.ALL,
								new CardCondition.Or(new CardCondition.Entity(EntityType.CREEPER),
										new CardCondition.Entity(EntityType.PHANTOM)))),
						new RemoveCardsAbility(), new PlaceCardsAbility(CardPlacement.ENEMY_HAND), new HistoryAbility(
								Items.BOOK.getDefaultInstance(), Optional.of(HistoryEntry.Visibility.ALL))))));
		add(new Builder(EntityType.SNOW_GOLEM, 4, 4, 4)
				.setCardAbility(
						new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("snow_golem"),
								List.of(new SelectCardsAbility(new CardAbilitySelection(
										new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
										new CardSelectionMethod.Choice(false), CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_snowball")),
										new ModifyAbility(List.of(new ModificationBuilder().heal(-2)
												.addProperty(CardProperty.FREEZE, 1).build())),
										history()))));
		add(new Builder(EntityType.CHICKEN, 2, 2, 1).setCardAbility(new ChanceAbility(textKey("chicken"), 50,
				addCards(EnumSet.of(CardAbilityTrigger.SUMMON), "",
						List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
								.setAdditionalData(new AdditionalCardData.ItemData(Items.EGG))
								.setCardAbility(new MultiAbility(
										textKey("egg"), List.of(
												new ChainAbility(
														EnumSet.of(CardAbilityTrigger.SUMMON), "", List.of(
																new SelectCardsAbility(new CardAbilitySelection(
																		new CardAbilityGroups(EnumSet
																				.of(CardAbilityGroup.ENEMY_BOARD)),
																		new CardSelectionMethod.Choice(false),
																		CardCondition.NoCondition.NO_CONDITION)),
																new AnimationAbility(mod("throw_egg")),
																new ModifyAbility(List.of(
																		new ModificationBuilder().heal(-1).build())),
																history())),
												summon(EnumSet.of(CardAbilityTrigger.SUMMON), "", CardPlacement.RIGHT,
														new LazyCardType(ForgeRegistries.ENTITY_TYPES
																.getKey(EntityType.CHICKEN))))))
								.build()))))));
		add(new Builder(EntityType.TURTLE, 5, 8, 1)
				.addProperty(CardProperty.BABY,
						1)
				.setCardAbility(
						addCards(EnumSet.of(CardAbilityTrigger.GROW), textKey("turtle"),
								List.of(new LazyCardType(
										new Builder(EntityType.ITEM, 0, 0, 0)
												.setAdditionalData(new AdditionalCardData.ItemData(Items.SCUTE))
												.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON),
														textKey("scute"), List.of(
																new SelectCardsAbility(new CardAbilitySelection(
																		new CardAbilityGroups(EnumSet
																				.of(CardAbilityGroup.YOUR_BOARD)),
																		new CardSelectionMethod.Choice(false),
																		CardCondition.NoCondition.NO_CONDITION)),
																new AnimationAbility(mod("throw_scute")),
																new ModifyAbility(List.of(new ModificationBuilder()
																		.addMaxHealth(5)
																		.setProperty(CardProperty.TAUNT, 1).build())),
																history())))
												.build())))));
		add(new Builder(EntityType.DROWNED, 7, 5, 9).addProperty(CardProperty.UNDEAD, 1).setCardAbility(addCards(
				EnumSet.of(CardAbilityTrigger.DEATH), textKey("drowned"), List.of(new LazyCardType(mod("trident"))))));
		add(new Builder(EntityType.COW, 2, 2, 2).setCardAbility(addCards(EnumSet.of(CardAbilityTrigger.SUMMON),
				textKey("cow"),
				List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
						.setAdditionalData(new AdditionalCardData.ItemData(Items.MILK_BUCKET))
						.setCardAbility(
								new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("milk_bucket"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD,
														CardAbilityGroup.ENEMY_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_milk")),
										new ModifyAbility(List.of(new ModificationBuilder()
												.setProperty(CardProperty.BURN, 0).setProperty(CardProperty.FREEZE, 0)
												.setProperty(CardProperty.SHIELD, 0)
												.setProperty(CardProperty.STEALTH, 0).setProperty(CardProperty.TAUNT, 0)
												.setProperty(CardProperty.THORNS, 0).setProperty(CardProperty.POISON, 0)
												.build())),
										history())))
						.build())))));
		add(new Builder(EntityType.FOX, 4, 3, 2).setCardAbility(summon(EnumSet.of(CardAbilityTrigger.SUMMON),
				textKey("fox"), CardPlacement.RIGHT, new LazyCardType(mod("sweet_berries")))));
		add(new Builder(EntityType.MOOSHROOM, 2, 2, 2)
				.setCardAbility(addCards(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("mooshroom"),
						List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
								.setAdditionalData(new AdditionalCardData.ItemData(Items.MUSHROOM_STEW))
								.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON),
										textKey("mushroom_stew"), List.of(
												new SelectCardsAbility(new CardAbilitySelection(
														new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD,
																CardAbilityGroup.ENEMY_BOARD)),
														new CardSelectionMethod.Choice(false),
														CardCondition.NoCondition.NO_CONDITION)),
												new AnimationAbility(mod("throw_mushroom_stew")),
												new ModifyAbility(List.of(new ModificationBuilder()
														.setProperty(CardProperty.BABY, 0).build())))))
								.build())))));
		add(new Builder(EntityType.PARROT, 1, 2, 1).addProperty(CardProperty.TAUNT, 1));
		add(new Builder(EntityType.LLAMA, 1, 1, 1)
				.setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("llama"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD,
														CardAbilityGroup.ENEMY_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("llama_spit")),
										new ModifyAbility(List.of(new ModificationBuilder().heal(-1).build())),
										history()))));
		add(new Builder(EntityType.CAT, 3, 3, 1)
				.setCardAbility(new ChanceAbility(textKey("cat"), 50, addCards(EnumSet.of(CardAbilityTrigger.TICK), "",
						List.of(new LazyCardType(mod("rotten_flesh")), new LazyCardType(mod("rabbit_foot")))))));
		add(new Builder(EntityType.CAVE_SPIDER, 3, 2, 2).setCardAbility(new ChanceAbility(textKey("cave_spider"), 50,
				new ChainAbility(EnumSet.of(CardAbilityTrigger.ATTACK), "", List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.TARGET)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(
								List.of(new ModificationBuilder().setProperty(CardProperty.POISON, 1).build())))))));
		add(new Builder(EntityType.WITCH, 6, 5, 4).setCardAbility(addCards(EnumSet.of(CardAbilityTrigger.SUMMON),
				textKey("witch"), List.of(new LazyCardType(mod("absorption_potion")),
						new LazyCardType(mod("poison_potion")), new LazyCardType(mod("healing_potion"))))));
		add(new Builder(EntityType.WANDERING_TRADER, 7, 7, 4).setCardAbility(new ChainAbility(
				Set.of(CardAbilityTrigger.SUMMON), textKey("wandering_trader"),
				List.of(new ConstantCardsAbility(List.of(new LazyCardType(mod("pufferfish_bucket")),
						new LazyCardType(mod("packed_ice")), new LazyCardType(mod("pointed_dripstone")))),
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.COLLECTED)),
										new CardSelectionMethod.Choice(true), CardCondition.NoCondition.NO_CONDITION),
								true),
						new PlaceCardsAbility(CardPlacement.YOUR_HAND), history()))));
		add(new Builder(EntityType.TRADER_LLAMA, 5, 4, 4)
				.setCardAbility(new ChoiceCardAbility(textKey("trader_llama"), List.of(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("trader_llama_spit"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD,
														CardAbilityGroup.ENEMY_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("llama_spit")),
										new ModifyAbility(List.of(new ModificationBuilder().heal(-2).build())),
										history())),
						new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("trader_llama_draw"),
								List.of(new DrawCardsAbility(Set.of(), "", 1), history()))))));
		add(new Builder(EntityType.DOLPHIN, 3, 2, 3).setCardAbility(summon(EnumSet.of(CardAbilityTrigger.SUMMON),
				textKey("dolphin"), CardPlacement.RIGHT, new LazyCardType(mod("buried_treasure")))));
		add(new Builder(EntityType.ZOMBIE_VILLAGER, 6, 4, 4).addProperty(CardProperty.UNDEAD, 1).setCardAbility(
				new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("zombie_villager"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD)),
										CardSelectionMethod.All.ALL,
										new CardCondition.OperatorCondition(new CardOperator.GreaterThan(
												new CardOperator.Variable(
														new CardVariable.PropertyVariable(CardProperty.UNDEAD)),
												new CardOperator.Constant(0))))),
						new ModifyAbility(List.of(new ModificationBuilder().addMaxHealth(2).addDamage(2).build())),
						new TriggerAdvancementAbility(new ResourceLocation(Main.MODID, "zombie_buff"),
								new CardCondition.OperatorCondition(new CardOperator.GreaterThan(
										new CardOperator.CollectedCount(0), new CardOperator.Constant(3)))),
						history()))));
		add(new Builder(EntityType.HOGLIN, 8, 5, 7).setCardAbility(new ChoiceCardAbility(textKey("hoglin"), List.of(
				summon(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("hoglin_baby"), CardPlacement.RIGHT,
						new LazyCardType(
								new Builder(EntityType.HOGLIN, 0, 3, 4).addProperty(CardProperty.BABY, 1).build())),
				new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("hoglin_buff"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(List.of(new ModificationBuilder().addMaxHealth(1).addDamage(1)
								.setProperty(CardProperty.TAUNT, 1).build())),
						history()))))));
		add(new Builder(EntityType.ZOGLIN, 9, 6, 12).addProperty(CardProperty.UNDEAD, 1)
				.setCardAbility(addCards(EnumSet.of(CardAbilityTrigger.DEATH), textKey("zoglin"),
						List.of(new LazyCardType(mod("rotten_flesh"))))));
		add(new Builder(EntityType.PIGLIN, 5, 4, 4).setCardAbility(addCards(EnumSet.of(CardAbilityTrigger.SUMMON),
				textKey("piglin"), List.of(new LazyCardType(mod("iron_boots")), new LazyCardType(mod("ender_pearl")),
						new LazyCardType(mod("fire_charge"))))));
		add(new Builder(EntityType.FROG, 3, 1, 1).setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON),
				textKey("frog"),
				List.of(new SelectCardsAbility(new CardAbilitySelection(
						new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
						new CardSelectionMethod.Choice(false),
						new CardCondition.OperatorCondition(new CardOperator.GreaterThan(new CardOperator.Constant(4),
								new CardOperator.Variable(CardVariable.MAX_HEALTH))))),
						new AnimationAbility(mod("frog_charge")), history(), new RemoveCardsAbility(),
						new MoveCollectedAbility(0, 1, true, false),
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(List.of(new ModificationBuilder()
								.put(new CardModification(CardVariable.MAX_HEALTH,
										new CardOperator.Add(new CardOperator.Variable(CardVariable.MAX_HEALTH),
												new CardOperator.CollectedAny(1,
														new CardOperator.Variable(CardVariable.MAX_HEALTH)))))
								.put(new CardModification(CardVariable.DAMAGE,
										new CardOperator.Add(new CardOperator.Variable(CardVariable.DAMAGE),
												new CardOperator.CollectedAny(1,
														new CardOperator.Variable(CardVariable.DAMAGE)))))
								.build()))))));

		// Spells
		add(new Builder(EntityType.ITEM, 4, 0, 0).setKey(mod("fishing_rod"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.FISHING_ROD))
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("fishing_rod"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_HAND)),
										new CardSelectionMethod.Choice(false), CardCondition.NoCondition.NO_CONDITION)),
						new RemoveCardsAbility(), new PlaceCardsAbility(CardPlacement.YOUR_HAND), new HistoryAbility(
								Items.BOOK.getDefaultInstance(), Optional.of(HistoryEntry.Visibility.ALL))))));
		add(new Builder(EntityType.ITEM, 3, 0, 0).setKey(mod("book"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.BOOK))
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("book"),
						List.of(new DrawCardsAbility(Set.of(), "", 2), history()))));
		add(new Builder(EntityType.ITEM, 3, 0, 0)
				.setKey(mod(
						"splash_potion_of_harming"))
				.setAdditionalData(new AdditionalCardData.ItemData(
						PotionUtils.setPotion(Items.SPLASH_POTION.getDefaultInstance(), Potions.HARMING)))
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON),
						textKey("splash_potion_of_harming"),
						List.of(new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
								new CardSelectionMethod.Random(2, false), CardCondition.NoCondition.NO_CONDITION)),
								new AnimationAbility(mod("throw_splash_potion_of_harming")),
								new ModifyAbility(List.of(new ModificationBuilder().heal(-3).build())), history()))));
		add(new Builder(EntityType.ITEM, 4, 0, 0).setKey(mod("enchanted_golden_apple"))
				.setAdditionalData(
						new AdditionalCardData.ItemData(Items.ENCHANTED_GOLDEN_APPLE))
				.setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("enchanted_golden_apple"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_enchanted_golden_apple")),
										new ModifyAbility(List.of(new ModificationBuilder().addMaxHealth(6)
												.setProperty(CardProperty.BURN, 0).setProperty(CardProperty.SHIELD, 1)
												.build())),
										history()))));
		add(new Builder(EntityType.ITEM, 1, 0, 0).setKey(mod("chest"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.CHEST))
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("chest"),
						List.of(new DrawCardsAbility(Set.of(), "", 3),
								new ModifyAbility(List.of(new ModificationBuilder().addCost(1).build())), history()))));
		add(new Builder(EntityType.ITEM, 2, 0, 0).setKey(mod("enchanted_book"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.ENCHANTED_BOOK))
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("enchanted_book"),
						List.of(new DrawCardsAbility(Set.of(), "", 1),
								new ModifyAbility(
										List.of(new ModificationBuilder().addDamage(2).addMaxHealth(2).build())),
								history()))));
		add(new Builder(EntityType.ITEM, 3, 0, 0)
				.setKey(mod(
						"spyglass"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.SPYGLASS))
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("spyglass"),
						List.of(new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_HAND)),
								new CardSelectionMethod.Random(3, false), CardCondition.NoCondition.NO_CONDITION),
								false),
								new SelectCardsAbility(new CardAbilitySelection(
										new CardAbilityGroups(EnumSet.of(CardAbilityGroup.COLLECTED)),
										new CardSelectionMethod.Choice(true), CardCondition.NoCondition.NO_CONDITION),
										true),
								new ModifyAbility(List.of(new ModificationBuilder().setCost(10).build())),
								new HistoryAbility(Items.BOOK.getDefaultInstance(),
										Optional.of(HistoryEntry.Visibility.ALL))))));
		add(new Builder(EntityType.ITEM, 8, 0, 0)
				.setKey(mod(
						"lodestone"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.LODESTONE))
				.setCardAbility(new ChainAbility(Set.of(CardAbilityTrigger.SUMMON), textKey("lodestone"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_HAND)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION),
								true),
						new RemoveCardsAbility(), new MoveCollectedAbility(0, 1, false, false),
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_HAND)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION),
								true),
						new RemoveCardsAbility(), new PlaceCardsAbility(CardPlacement.ENEMY_HAND),
						new MoveCollectedAbility(0, 2, false, false), new MoveCollectedAbility(1, 0, false, true),
						new PlaceCardsAbility(CardPlacement.YOUR_HAND), new MoveCollectedAbility(2, 0, false, false),
						new HistoryAbility(Items.BOOK.getDefaultInstance(),
								Optional.of(HistoryEntry.Visibility.ALL))))));
		add(new Builder(EntityType.ITEM, 5, 0, 0)
				.setKey(mod(
						"soul_sand"))
				.setAdditionalData(
						new AdditionalCardData.ItemData(Items.SOUL_SAND))
				.setCardAbility(
						new ChainAbility(
								Set.of(CardAbilityTrigger.SUMMON), textKey("soul_sand"), List.of(
										new SelectCardsAbility(
												new CardAbilitySelection(
														new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
														CardSelectionMethod.All.ALL,
														CardCondition.NoCondition.NO_CONDITION),
												true),
										new AnimationAbility(mod(
												"soul_sand")),
										new ModifyAbility(List.of(new ModificationBuilder()
												.put(new CardModification(CardVariable.HEALTH,
														new CardOperator.Negate(new CardOperator.CollectedCount(0))))
												.build())),
										history()))));
		add(new Builder(EntityType.ITEM, 2, 0, 0).setKey(mod("amethyst_shard"))
				.setAdditionalData(
						new AdditionalCardData.ItemData(Items.AMETHYST_SHARD))
				.setCardAbility(
						new ChainAbility(
								Set.of(CardAbilityTrigger.SUMMON), textKey("amethyst_shard"), List
										.of(new DrawCardsAbility(Set.of(), "", 1),
												new MoveCollectedAbility(0, 1, true, false),
												new SelectCardsAbility(
														new CardAbilitySelection(
																new CardAbilityGroups(
																		EnumSet.of(CardAbilityGroup.ENEMY_BOARD)),
																new CardSelectionMethod.Random(1, false),
																CardCondition.NoCondition.NO_CONDITION),
														true),
												new AnimationAbility(mod("throw_amethyst_shard")),
												new ModifyAbility(
														List.of(new ModificationBuilder()
																.put(new CardModification(CardVariable.HEALTH,
																		new CardOperator.Negate(
																				new CardOperator.CollectedAny(1,
																						new CardOperator.Variable(
																								CardVariable.COST)))))
																.build())),
												new MoveCollectedAbility(1, 0, false, false), history()))));
		add(new Builder(EntityType.ITEM, 2, 0, 0).setKey(mod("wooden_sword"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.WOODEN_SWORD)).setCardAbility(
						new ChainAbility(
								Set.of(CardAbilityTrigger.SUMMON), textKey("wooden_sword"), List.of(
										new ConstantCardsAbility(
												List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
														.setCardAbility(new ChainAbility(
																Set.of(CardAbilityTrigger.DEATH),
																textKey("wooden_sword_return"),
																List.of(new ConstantCardsAbility(
																		List.of(new LazyCardType(mod("wooden_sword")))),
																		new PlaceCardsAbility(CardPlacement.YOUR_HAND),
																		new HistoryAbility(
																				Items.BOOK.getDefaultInstance(),
																				Optional.of(
																						HistoryEntry.Visibility.ALL)))))
														.build()))),
										new MoveCollectedAbility(0, 1, true, true),
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION), true),
										new ModifyAbility(List.of(new ModificationBuilder().addDamage(3).build())),
										new AddAbilityAbility(1), history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0)
				.setKey(mod(
						"slime_ball"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.SLIME_BALL))
				.setCardAbility(new ChainAbility(Set.of(CardAbilityTrigger.SUMMON), textKey("slime_ball"), List.of(
						new DrawCardsAbility(Set.of(), "", 2), new HistoryAbility(Items.BOOK),
						new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.COLLECTED)),
								new CardSelectionMethod.Random(1, false), CardCondition.NoCondition.NO_CONDITION),
								true),
						new PlaceCardsAbility(CardPlacement.ENEMY_HAND)))));
		add(new Builder(EntityType.ITEM, 1, 0, 0).setKey(mod("bow"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.BOW))
				.setCardAbility(new ChainAbility(Set.of(CardAbilityTrigger.SUMMON), textKey("bow"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(
										new CardAbilityGroups(
												EnumSet.of(CardAbilityGroup.ENEMY_BOARD, CardAbilityGroup.YOUR_BOARD)),
										new CardSelectionMethod.Choice(false), CardCondition.NoCondition.NO_CONDITION),
								true),
						new AnimationAbility((mod("shoot_arrow"))), new HistoryAbility(Items.BOOK),
						new ModifyAbility(List.of(new ModificationBuilder().heal(-3).build())),
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION),
								true),
						new ModifyAbility(List.of(new ModificationBuilder().addCost(1).build())),
						new PlaceCardsAbility(CardPlacement.YOUR_DECK)))));
		add(new Builder(EntityType.ITEM, 4, 0, 0).setKey(mod("pumpkin"))
				.setAdditionalData(
						new AdditionalCardData.ItemData(Items.PUMPKIN))
				.setCardAbility(
						new ChoiceCardAbility(textKey("pumpkin"), List.of(
								new ChainAbility(
										EnumSet.of(CardAbilityTrigger.SUMMON), textKey("pumpkin_taunt"), List.of(
												new SelectCardsAbility(new CardAbilitySelection(new CardAbilityGroups(
														EnumSet.of(CardAbilityGroup.YOUR_BOARD)),
														new CardSelectionMethod.Choice(false),
														CardCondition.NoCondition.NO_CONDITION)),
												new AnimationAbility(mod("throw_pumpkin")),
												new ModifyAbility(
														List.of(new ModificationBuilder().addDamage(5).addMaxHealth(5)
																.setProperty(CardProperty.TAUNT, 1).build())),
												history())),
								new ChainAbility(
										EnumSet.of(CardAbilityTrigger.SUMMON), textKey("pumpkin_summon"), List.of(
												new ConstantCardsAbility(
														List.of(new LazyCardType(mod("taunt_snow_golem")),
																new LazyCardType(mod("taunt_snow_golem")))),
												new PlaceCardsAbility(CardPlacement.LEFT), history()))))));

		// Auxiliary cards
		add(new Builder(EntityType.ITEM, 0, 5, 0).setKey(mod("end_crystal"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.END_CRYSTAL))
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.TICK), textKey("end_crystal"),
						List.of(new SelectCardsAbility(new CardAbilitySelection(
								new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ADJACENT)),
								CardSelectionMethod.All.ALL, new CardCondition.Entity(EntityType.ENDER_DRAGON))),
								new ModifyAbility(List.of(new ModificationBuilder().heal(4).build())), history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("trident"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.TRIDENT)).setCardAbility(
						new MultiAbility(textKey("trident"), List.of(
								new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), "", List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_trident")),
										new ModifyAbility(List.of(new ModificationBuilder().heal(-4)
												.addProperty(CardProperty.BURN, 4).build())),
										history())),
								new ChanceAbility("", 30, addCards(EnumSet.of(CardAbilityTrigger.SUMMON), "",
										List.of(new LazyCardType(mod("trident")))))))));
		add(new Builder(EntityType.ITEM, 0, 3, 0).setKey(mod("sweet_berries"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.SWEET_BERRIES))
				.addProperty(CardProperty.THORNS, 2)
				.setCardAbility(addCards(EnumSet.of(CardAbilityTrigger.DEATH), textKey("sweet_berries"),
						List.of(new LazyCardType(new Builder(EntityType.ITEM, 0, 0, 0)
								.setAdditionalData(new AdditionalCardData.ItemData(Items.SWEET_BERRIES))
								.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON),
										textKey("throw_sweet_berries"), List.of(
												new SelectCardsAbility(new CardAbilitySelection(
														new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
																CardAbilityGroup.YOUR_BOARD)),
														new CardSelectionMethod.Choice(false),
														CardCondition.NoCondition.NO_CONDITION)),
												new AnimationAbility(mod("throw_sweet_berries")),
												new ModifyAbility(List.of(new ModificationBuilder().heal(3).build())),
												history())))
								.build())))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("rotten_flesh"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.ROTTEN_FLESH)).setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("rotten_flesh"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_rotten_flesh")),
										new ModifyAbility(List.of(
												new ModificationBuilder().random(CardVariable.HEALTH, -2, 2).build())),
										history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("rabbit_foot"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.RABBIT_FOOT)).setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("rabbit_foot"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_rabbit_foot")),
										new ModifyAbility(List.of(new ModificationBuilder().addMaxHealth(1).build())),
										history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("absorption_potion"))
				.setAdditionalData(new AdditionalCardData.ItemData(
						PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.TURTLE_MASTER)))
				.setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("absorption_potion"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_absorption_potion")),
										new ModifyAbility(List.of(
												new ModificationBuilder().setProperty(CardProperty.SHIELD, 1).build())),
										history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("poison_potion"))
				.setAdditionalData(new AdditionalCardData.ItemData(
						PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.POISON)))
				.setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("poison_potion"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_poison_potion")),
										new ModifyAbility(List.of(
												new ModificationBuilder().setProperty(CardProperty.POISON, 1).build())),
										history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("healing_potion"))
				.setAdditionalData(new AdditionalCardData.ItemData(
						PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.HEALING)))
				.setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("healing_potion"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_healing_potion")),
										new ModifyAbility(List.of(new ModificationBuilder().heal(5).build())),
										history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("pufferfish_bucket"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.PUFFERFISH_BUCKET)).setCardAbility(
						summon(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("pufferfish_bucket"), CardPlacement.RIGHT,
								new LazyCardType(ForgeRegistries.ENTITY_TYPES.getKey(EntityType.PUFFERFISH)))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("packed_ice"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.PACKED_ICE)).setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("packed_ice"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_packed_ice")),
										new ModifyAbility(List.of(
												new ModificationBuilder().addProperty(CardProperty.FREEZE, 2).build())),
										history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("pointed_dripstone"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.POINTED_DRIPSTONE)).setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("pointed_dripstone"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_pointed_dripstone")),
										new ModifyAbility(List.of(
												new ModificationBuilder().addProperty(CardProperty.THORNS, 3).build())),
										history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("iron_sword"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.IRON_SWORD)).setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("iron_sword"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_iron_sword")),
										new ModifyAbility(List.of(new ModificationBuilder().addDamage(2).build())),
										history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("emerald"))
				.setCardAbility(new MultiAbility(textKey("emerald"),
						List.of(new ResourceAbility(Set.of(CardAbilityTrigger.SUMMON), "", 1, 0),
								new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), "", List.of(history())))))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.EMERALD)));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("leather_chestplate"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.LEATHER_CHESTPLATE)).setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("leather_chestplate"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_leather_chestplate")),
										new ModifyAbility(List.of(new ModificationBuilder().addMaxHealth(2).build())),
										history()))));
		add(new Builder(EntityType.ITEM, 0, 2, 0).setKey(mod("buried_treasure"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.CHEST.getDefaultInstance()
						.setHoverName(Component.translatable(Helper.gui("buried_treasure")))))
				.setCardAbility(addCards(EnumSet.of(CardAbilityTrigger.DEATH), textKey("buried_treasure"),
						List.of(new LazyCardType(mod("emerald")), new LazyCardType(mod("leather_chestplate")),
								new LazyCardType(mod("iron_sword"))))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("iron_boots"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.IRON_BOOTS)).setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("iron_boots"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("throw_iron_boots")),
										new ModifyAbility(List.of(new ModificationBuilder().addMaxHealth(2).build())),
										history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0).setKey(mod("fire_charge"))
				.setAdditionalData(new AdditionalCardData.ItemData(Items.FIRE_CHARGE)).setCardAbility(
						new ChainAbility(
								EnumSet.of(CardAbilityTrigger.SUMMON), textKey("fire_charge"), List.of(
										new SelectCardsAbility(new CardAbilitySelection(
												new CardAbilityGroups(EnumSet.of(CardAbilityGroup.ENEMY_BOARD,
														CardAbilityGroup.YOUR_BOARD)),
												new CardSelectionMethod.Choice(false),
												CardCondition.NoCondition.NO_CONDITION)),
										new AnimationAbility(mod("fireball")),
										new ModifyAbility(List.of(
												new ModificationBuilder().addProperty(CardProperty.BURN, 3).build())),
										history()))));
		add(new Builder(EntityType.ITEM, 0, 0, 0)
				.setKey(mod(
						"ender_pearl"))
				.setAdditionalData(
						new AdditionalCardData.ItemData(Items.ENDER_PEARL))
				.setCardAbility(new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), textKey("ender_pearl"), List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.YOUR_BOARD)),
										new CardSelectionMethod.Choice(false),
										new CardCondition.Not(new CardCondition.Entity(EntityType.PLAYER)))),
						new AnimationAbility(mod("throw_ender_pearl")), new RemoveCardsAbility(),
						new ModifyAbility(List.of(new ModificationBuilder().restore().build())),
						new PlaceCardsAbility(CardPlacement.YOUR_HAND), new HistoryAbility(
								Items.BOOK.getDefaultInstance(), Optional.of(HistoryEntry.Visibility.ALL))))));
		add(new Builder(EntityType.SNOW_GOLEM, 0, 2, 2).setKey(mod("taunt_snow_golem")).addProperty(CardProperty.TAUNT,
				1));

		printStatistics();
	}

	private void printStatistics() {
		Map<Integer, Integer> costCounts = new TreeMap<>();
		Map<ResourceLocation, Integer> propertyCounts = new TreeMap<>();
		Set<EntityType<?>> types = new HashSet<>();
		for (var entry : cards.entrySet()) {
			var card = entry.getValue();
			types.add(card.getType());
			if (Cards.isAllowed(card.getType(), FeatureFlags.DEFAULT_FLAGS)
					&& ForgeRegistries.ENTITY_TYPES.containsKey(entry.getKey())) {
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
			System.out.println(entry.getKey() + ": " + entry.getValue());

		for (var entry : ForgeRegistries.ENTITY_TYPES.getEntries()) {
			if (Cards.isAllowed(entry.getValue(), FeatureFlags.DEFAULT_FLAGS) && !types.contains(entry.getValue())) {
				System.out.println("Missing " + entry.getKey());
			}
		}
	}

	private CardAbility summon(Set<CardAbilityTrigger> triggers, String textKey, CardPlacement placement,
			LazyCardType summon) {
		return new ChainAbility(triggers, textKey,
				List.of(new ConstantCardsAbility(List.of(summon)), new PlaceCardsAbility(placement), history()));
	}

	public CardAbility addCards(Set<CardAbilityTrigger> triggers, String textKey, List<LazyCardType> toAdd) {
		List<CardAbility> abilities = new ArrayList<>();
		abilities.add(new ConstantCardsAbility(toAdd));
		if (toAdd.size() > 1)
			abilities
					.add(new SelectCardsAbility(
							new CardAbilitySelection(new CardAbilityGroups(Set.of(CardAbilityGroup.COLLECTED)),
									new CardSelectionMethod.Random(1, false), CardCondition.NoCondition.NO_CONDITION),
							true));
		abilities.add(new PlaceCardsAbility(CardPlacement.YOUR_HAND));
		abilities.add(new HistoryAbility(Items.BOOK.getDefaultInstance(),
				toAdd.size() == 1 ? Optional.of(HistoryEntry.Visibility.ALL) : Optional.empty()));
		return new ChainAbility(triggers, textKey, abilities);
	}

	private void splitter(EntityType<?> entity, int cost, CardAbility ability) {
		var name = ForgeRegistries.ENTITY_TYPES.getKey(entity).getPath();
		var small = mod("small_" + name);
		var medium = mod("medium_" + name);
		var textKey = textKey("splitter");
		add(new Builder(entity, 0, 1, 1).setKey(small).setCardAbility(ability));
		add(new Builder(entity, 0, 2, 2).setKey(medium).setCardAbility(new MultiAbility("",
				List.of(ability, new ChainAbility(EnumSet.of(CardAbilityTrigger.DEATH), textKey,
						List.of(new ConstantCardsAbility(List.of(new LazyCardType(small))),
								new PlaceCardsAbility(CardPlacement.LEFT), new PlaceCardsAbility(CardPlacement.RIGHT),
								new ConstantCardsAbility(List.of(new LazyCardType(small))), history()))))));
		add(new Builder(entity, cost, 4, 4).setCardAbility(new MultiAbility("",
				List.of(ability, new ChainAbility(EnumSet.of(CardAbilityTrigger.DEATH), textKey,
						List.of(new ConstantCardsAbility(List.of(new LazyCardType(medium))),
								new PlaceCardsAbility(CardPlacement.LEFT), new PlaceCardsAbility(CardPlacement.RIGHT),
								new ConstantCardsAbility(List.of(new LazyCardType(medium))), history()))))));
	}

	private Builder cod(int chance) {
		if (chance == 0)
			return new Builder(EntityType.COD, 1, 1, 1);

		return new Builder(EntityType.COD, 1, 1, 1)
				.setCardAbility(new ChanceAbility(textKey("cod"), chance, summon(EnumSet.of(CardAbilityTrigger.SUMMON),
						"", CardPlacement.RIGHT, new LazyCardType(cod(chance - 10).build()))));
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

	private static class ModificationBuilder {
		List<CardModification> modifications = new ArrayList<>();

		private ModificationBuilder addCost(int value) {
			modifications.add(new CardModification(CardVariable.COST, new CardOperator.Add(
					new CardOperator.Variable(CardVariable.COST), new CardOperator.Constant(value))));
			return this;
		}

		private ModificationBuilder setCost(int value) {
			modifications.add(new CardModification(CardVariable.COST, new CardOperator.Constant(value)));
			return this;
		}

		private ModificationBuilder addDamage(int value) {
			modifications.add(new CardModification(CardVariable.DAMAGE, new CardOperator.Add(
					new CardOperator.Variable(CardVariable.DAMAGE), new CardOperator.Constant(value))));
			return this;
		}

		private ModificationBuilder addMaxHealth(int value) {
			modifications.add(new CardModification(CardVariable.MAX_HEALTH, new CardOperator.Add(
					new CardOperator.Variable(CardVariable.MAX_HEALTH), new CardOperator.Constant(value))));
			return this;
		}

		private ModificationBuilder setMaxHealth(int value) {
			modifications.add(new CardModification(CardVariable.MAX_HEALTH, new CardOperator.Constant(value)));
			return this;
		}

		private ModificationBuilder heal(int value) {
			modifications.add(new CardModification(CardVariable.HEALTH, new CardOperator.Constant(value)));
			return this;
		}

		private ModificationBuilder restore() {
			modifications
					.add(new CardModification(CardVariable.HEALTH, new CardOperator.Variable(CardVariable.MAX_HEALTH)));
			return this;
		}

		private ModificationBuilder setProperty(ResourceLocation property, int value) {
			modifications.add(new CardModification(new CardVariable.PropertyVariable(property),
					new CardOperator.Constant(value)));
			return this;
		}

		private ModificationBuilder addProperty(ResourceLocation property, int value) {
			addProperty(property, new CardOperator.Constant(value));
			return this;
		}

		private ModificationBuilder addProperty(ResourceLocation property, CardOperator operator) {
			modifications.add(new CardModification(new CardVariable.PropertyVariable(property), new CardOperator.Add(
					new CardOperator.Variable(new CardVariable.PropertyVariable(property)), operator)));
			return this;
		}

		private ModificationBuilder random(CardVariable variable, int min, int max) {
			modifications.add(new CardModification(variable,
					new CardOperator.RandomOperator(new CardOperator.Constant(min), new CardOperator.Constant(max))));
			return this;
		}

		private ModificationBuilder put(CardModification mod) {
			modifications.add(mod);
			return this;
		}

		private List<CardModification> build() {
			return modifications;
		}
	}

	private static class Builder {

		private EntityType<?> type;
		private int cost;
		private int health;
		private int damage;
		private Map<ResourceLocation, Integer> properties = new HashMap<>();
		private CardAbility ability = NoCardAbility.NO_CARD_ABILITY;
		private AdditionalCardData additionalData = AdditionalCardData.EMPTY;
		private int deckCount = CardType.DEFAULT_DECK_COUNT;
		private float dropChance = CardType.DEFAULT_DROP_CHANCE;
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

		private Builder addProperty(ResourceLocation property, int value) {
			properties.put(property, value);
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

		private Builder setDeckCount(int deckCount) {
			this.deckCount = deckCount;
			return this;
		}

		private Builder setDropChance(float dropChance) {
			this.dropChance = dropChance;
			return this;
		}

		private CardType build() {
			return new CardType(type, cost, health, damage, properties, ability, additionalData, deckCount, dropChance);
		}

		private ResourceLocation getKey() {
			return key == null ? ForgeRegistries.ENTITY_TYPES.getKey(type) : key;
		}
	}

}
