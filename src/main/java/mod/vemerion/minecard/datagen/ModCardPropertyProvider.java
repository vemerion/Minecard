package mod.vemerion.minecard.datagen;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.CardProperties;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.ability.CardAbilityGroup;
import mod.vemerion.minecard.game.ability.CardAbilityGroups;
import mod.vemerion.minecard.game.ability.CardAbilitySelection;
import mod.vemerion.minecard.game.ability.CardAbilityTrigger;
import mod.vemerion.minecard.game.ability.CardCondition;
import mod.vemerion.minecard.game.ability.CardModification;
import mod.vemerion.minecard.game.ability.CardOperator;
import mod.vemerion.minecard.game.ability.CardSelectionMethod;
import mod.vemerion.minecard.game.ability.CardVariable;
import mod.vemerion.minecard.game.ability.ChainAbility;
import mod.vemerion.minecard.game.ability.ModifyAbility;
import mod.vemerion.minecard.game.ability.MultiAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import mod.vemerion.minecard.game.ability.SelectCardsAbility;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModCardPropertyProvider implements DataProvider {
	private Map<ResourceLocation, CardProperty> properties = new HashMap<>();
	private PackOutput packOutput;

	public ModCardPropertyProvider(PackOutput packOutput) {
		this.packOutput = packOutput;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		addProperties();
		var folder = packOutput.getOutputFolder();
		var list = new ArrayList<CompletableFuture<?>>();
		for (var entry : properties.entrySet()) {
			var key = entry.getKey();
			var path = folder.resolve(
					"data/" + key.getNamespace() + "/" + CardProperties.FOLDER_NAME + "/" + key.getPath() + ".json");
			list.add(DataProvider.saveStable(cache,
					CardProperty.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, s -> {
					}), path));
		}
		return CompletableFuture.allOf(list.toArray((length) -> {
			return new CompletableFuture[length];
		}));
	}

	private void addProperties() {
		properties.put(CardProperty.TAUNT,
				new CardProperty(new ItemStack(Items.CARROT_ON_A_STICK), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.CHARGE, new CardProperty(new ItemStack(Items.SUGAR),
				new ChainAbility(EnumSet.of(CardAbilityTrigger.SUMMON), "", List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(

								List.of(List.of(new CardModification(
										new CardVariable.PropertyVariable(CardProperty.READY),
										new CardOperator.Add(new CardOperator.Constant(1), new CardOperator.Variable(
												new CardVariable.PropertyVariable(CardProperty.ECHO)))))))))));
		properties.put(CardProperty.STEALTH,
				new CardProperty(new ItemStack(Items.TALL_GRASS), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.FREEZE, new CardProperty(new ItemStack(Items.ICE), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.SHIELD,
				new CardProperty(new ItemStack(Items.DIAMOND_CHESTPLATE), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.BURN, new CardProperty(new ItemStack(Items.LAVA_BUCKET),
				new ChainAbility(EnumSet.of(CardAbilityTrigger.TICK), "", List.of(
						new SelectCardsAbility(
								new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
										CardSelectionMethod.All.ALL, CardCondition.NoCondition.NO_CONDITION)),
						new ModifyAbility(

								List.of(List.of(
										new CardModification(CardVariable.HEALTH, new CardOperator.Constant(-1)),
										new CardModification(new CardVariable.PropertyVariable(CardProperty.BURN),
												new CardOperator.Add(
														new CardOperator.Variable(
																new CardVariable.PropertyVariable(CardProperty.BURN)),
														new CardOperator.Constant(-1))))))))));
		properties.put(CardProperty.SPECIAL,
				new CardProperty(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.BABY, new CardProperty(new ItemStack(Items.EGG), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.THORNS,
				new CardProperty(new ItemStack(Items.POINTED_DRIPSTONE), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.POISON, new CardProperty(new ItemStack(Items.SPIDER_EYE), new ChainAbility(
				EnumSet.of(CardAbilityTrigger.TICK), "",
				List.of(new SelectCardsAbility(new CardAbilitySelection(
						new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)), CardSelectionMethod.All.ALL,
						new CardCondition.OperatorCondition(new CardOperator.GreaterThan(
								new CardOperator.Variable(CardVariable.HEALTH), new CardOperator.Constant(1))))),
						new ModifyAbility(

								List.of(List.of(
										new CardModification(CardVariable.HEALTH, new CardOperator.Constant(-1)))))))));
		properties.put(CardProperty.UNDEAD,
				new CardProperty(new ItemStack(Items.ZOMBIE_HEAD), NoCardAbility.NO_CARD_ABILITY));
		properties
				.put(CardProperty.READY,
						new CardProperty(ItemStack.EMPTY,
								new MultiAbility(
										"", List.of(
												new ChainAbility(EnumSet.of(CardAbilityTrigger.ATTACK), "", List
														.of(new SelectCardsAbility(new CardAbilitySelection(
																new CardAbilityGroups(
																		EnumSet.of(CardAbilityGroup.SELF)),
																CardSelectionMethod.All.ALL,
																CardCondition.NoCondition.NO_CONDITION)),
																new ModifyAbility(List.of(List.of(new CardModification(
																		new CardVariable.PropertyVariable(
																				CardProperty.READY),
																		new CardOperator.Add(new CardOperator.Variable(
																				new CardVariable.PropertyVariable(
																						CardProperty.READY)),
																				new CardOperator.Constant(-1)))))))),
												new ChainAbility(EnumSet.of(CardAbilityTrigger.TICK), "", List.of(
														new SelectCardsAbility(
																new CardAbilitySelection(
																		new CardAbilityGroups(EnumSet
																				.of(CardAbilityGroup.SELF)),
																		CardSelectionMethod.All.ALL,
																		CardCondition.NoCondition.NO_CONDITION)),
														new ModifyAbility(List.of(List.of(new CardModification(
																new CardVariable.PropertyVariable(CardProperty.READY),
																new CardOperator.Constant(0)))))))))));
		properties.put(CardProperty.ADVANCEMENT_COUNTER,
				new CardProperty(ItemStack.EMPTY, NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.ECHO,
				new CardProperty(new ItemStack(Items.AMETHYST_SHARD), NoCardAbility.NO_CARD_ABILITY));

	}

	@Override
	public String getName() {
		return Main.MODID + ": Card Properties";
	}

}
