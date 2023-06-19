package mod.vemerion.minecard.datagen;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import mod.vemerion.minecard.game.ability.ModifyAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModCardPropertyProvider implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private Map<ResourceLocation, CardProperty> properties = new HashMap<>();
	private DataGenerator generator;

	public ModCardPropertyProvider(DataGenerator generator) {
		this.generator = generator;
	}

	@Override
	public void run(HashCache cache) throws IOException {
		addProperties();
		var folder = generator.getOutputFolder();
		for (var entry : properties.entrySet()) {
			var key = entry.getKey();
			var path = folder.resolve(
					"data/" + key.getNamespace() + "/" + CardProperties.FOLDER_NAME + "/" + key.getPath() + ".json");
			try {
				DataProvider.save(GSON, cache,
						CardProperty.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, s -> {
						}), path);
			} catch (IOException e) {
				Main.LOGGER.error("Couldn't save card property " + path + ": " + e);
			}
		}
	}

	private void addProperties() {
		properties.put(CardProperty.TAUNT,
				new CardProperty(new ItemStack(Items.CARROT_ON_A_STICK), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.CHARGE,
				new CardProperty(new ItemStack(Items.SUGAR), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.STEALTH,
				new CardProperty(new ItemStack(Items.TALL_GRASS), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.FREEZE, new CardProperty(new ItemStack(Items.ICE), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.SHIELD,
				new CardProperty(new ItemStack(Items.DIAMOND_CHESTPLATE), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.BURN, new CardProperty(new ItemStack(Items.LAVA_BUCKET), new ModifyAbility(
				CardAbilityTrigger.TICK, Optional.empty(),
				new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
						CardSelectionMethod.ALL, CardCondition.NoCondition.NO_CONDITION),
				List.of(List.of(new CardModification(CardVariable.HEALTH, new CardOperator.Constant(-1)),
						new CardModification(new CardVariable.PropertyVariable(CardProperty.BURN),
								new CardOperator.Add(
										new CardOperator.Variable(new CardVariable.PropertyVariable(CardProperty.BURN)),
										new CardOperator.Constant(-1))))))));
		properties.put(CardProperty.SPECIAL,
				new CardProperty(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.BABY, new CardProperty(new ItemStack(Items.EGG), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.THORNS,
				new CardProperty(new ItemStack(Items.POINTED_DRIPSTONE), NoCardAbility.NO_CARD_ABILITY));
		properties.put(CardProperty.POISON, new CardProperty(new ItemStack(Items.SPIDER_EYE),
				new ModifyAbility(CardAbilityTrigger.TICK, Optional.empty(),
						new CardAbilitySelection(new CardAbilityGroups(EnumSet.of(CardAbilityGroup.SELF)),
								CardSelectionMethod.ALL,
								new CardCondition.OperatorCondition(new CardOperator.GreaterThan(
										new CardOperator.Variable(CardVariable.HEALTH), new CardOperator.Constant(1)))),
						List.of(List.of(new CardModification(CardVariable.HEALTH, new CardOperator.Constant(-1)))))));
		properties.put(CardProperty.UNDEAD,
				new CardProperty(new ItemStack(Items.ZOMBIE_HEAD), NoCardAbility.NO_CARD_ABILITY));
	}

	@Override
	public String getName() {
		return Main.MODID + ": Card Properties";
	}

}
