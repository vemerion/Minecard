package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import mod.vemerion.minecard.capability.DeckData;
import mod.vemerion.minecard.game.ability.AddCardsAbility;
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
import mod.vemerion.minecard.game.ability.ChoiceCardAbility;
import mod.vemerion.minecard.game.ability.CopyCardsAbility;
import mod.vemerion.minecard.game.ability.DrawCardsAbility;
import mod.vemerion.minecard.game.ability.ModifyAbility;
import mod.vemerion.minecard.game.ability.MultiAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import mod.vemerion.minecard.game.ability.ResourceAbility;
import mod.vemerion.minecard.game.ability.SelectCardsAbility;
import mod.vemerion.minecard.game.ability.SummonCardAbility;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

// For testing
public class RandomCardGenerator {

	private Random rand;
	private int maxDepth;

	public RandomCardGenerator(Random rand, int maxDepth) {
		this.rand = rand;
		this.maxDepth = maxDepth;
	}

	public static List<Card> generateDeck() {
		List<Card> deck = new ArrayList<>();
		var gen = new RandomCardGenerator(new Random(), 4);
		for (int i = 0; i < DeckData.CAPACITY; i++)
			deck.add(gen.next().create());

		return deck;
	}

	public CardType next() {
		return next(0);
	}

	private <T extends Enum<T>> T randEnum(Class<T> t) {
		return t.getEnumConstants()[rand.nextInt(t.getEnumConstants().length)];
	}

	private CardAbilityGroups randGroups() {
		Set<CardAbilityGroup> groups = new HashSet<>();
		for (int i = 0; i < rand.nextInt(1, 5); i++) {
			var val = randEnum(CardAbilityGroup.class);
			boolean invalid = false;
			for (var incompatible : CardAbilityGroups.INCOMPATIBLE) {
				var left = incompatible.getLeft();
				var right = incompatible.getRight();
				if ((groups.contains(left) || groups.contains(right)) && (val == left || val == right)) {
					invalid = true;
					break;
				}
			}
			if (!invalid && !groups.contains(CardAbilityGroup.ALL)
					&& (val != CardAbilityGroup.ALL || groups.isEmpty())) {
				groups.add(val);
			}
		}

		return new CardAbilityGroups(groups);
	}

	private List<CardModification> randModifications() {
		List<CardModification> result = new ArrayList<>();

		for (int i = 0; i < rand.nextInt(1, 5); i++) {
			switch (rand.nextInt(5)) {
			case 0:
				result.add(new CardModification(CardVariable.COST, new CardOperator.Constant(rand.nextInt(0, 10))));
				break;
			case 1:
				result.add(new CardModification(CardVariable.DAMAGE, new CardOperator.Constant(rand.nextInt(0, 10))));
				break;
			case 2:
				result.add(new CardModification(CardVariable.HEALTH, new CardOperator.Constant(rand.nextInt(-5, 5))));
				break;
			case 3:
				result.add(
						new CardModification(CardVariable.MAX_HEALTH, new CardOperator.Constant(rand.nextInt(1, 10))));
				break;
			case 4:
				result.add(new CardModification(
						new CardVariable.PropertyVariable(CardProperties.getInstance(false).randomKey(rand)),
						new CardOperator.Constant(rand.nextInt(0, 3))));
				break;
			}
		}

		return result;
	}

	private Set<CardAbilityTrigger> randTriggers() {
		Set<CardAbilityTrigger> result = EnumSet.noneOf(CardAbilityTrigger.class);
		for (int i = 0; i < rand.nextInt(1, 4); i++) {
			result.add(randEnum(CardAbilityTrigger.class));
		}
		return result;
	}

	private CardType next(int depth) {
		var entities = ForgeRegistries.ENTITIES.getValues();
		var type = entities.stream().skip(rand.nextInt(entities.size())).findFirst().get();
		var cost = rand.nextInt(11);
		var health = rand.nextInt(3);
		var damage = rand.nextInt(3);
		Map<ResourceLocation, Integer> properties = new HashMap<>();
		for (int i = 0; i < rand.nextInt(3); i++) {
			properties.put(CardProperties.getInstance(false).randomKey(rand), rand.nextInt(1, 3));
		}

		List<CardAbility> abilities = new ArrayList<>();
		abilities.add(NoCardAbility.NO_CARD_ABILITY);
		if (depth < maxDepth) {
			abilities.add(new AddCardsAbility(randTriggers(), List.of(new LazyCardType(next(depth + 1)))));
			abilities.add(new ChoiceCardAbility(
					IntStream.range(0, rand.nextInt(1, 5)).mapToObj(i -> next(depth + 1).getAbility())
							.collect(Collectors.toCollection(() -> new ArrayList<>()))));
			abilities.add(new DrawCardsAbility(randTriggers(), rand.nextInt(1, 3)));
			abilities.add(new ResourceAbility(randTriggers(), rand.nextInt(3), rand.nextInt(3)));
			abilities.add(new SummonCardAbility(randTriggers(),
					CardPlacement.values()[rand.nextInt(CardPlacement.values().length)],
					new LazyCardType(next(depth + 1))));
			abilities.add(new ChainAbility(randTriggers(),
					List.of(new SelectCardsAbility(new CardAbilitySelection(randGroups(),
							randEnum(CardSelectionMethod.class), CardCondition.NoCondition.NO_CONDITION)),
							new CopyCardsAbility(rand.nextBoolean(), rand.nextBoolean(), rand.nextBoolean(),
									Optional.empty()))));
			abilities.add(new ChainAbility(randTriggers(),
					List.of(new SelectCardsAbility(new CardAbilitySelection(randGroups(),
							randEnum(CardSelectionMethod.class), CardCondition.NoCondition.NO_CONDITION)),
							new ModifyAbility(Optional.empty(), List.of(randModifications())))));
			abilities.add(
					new MultiAbility(IntStream.range(0, rand.nextInt(1, 5)).mapToObj(i -> next(depth + 1).getAbility())
							.collect(Collectors.toCollection(() -> new ArrayList<>()))));
		}

		return new CardType(type, cost, health, damage, properties, abilities.get(rand.nextInt(abilities.size())),
				AdditionalCardData.EMPTY, CardType.DEFAULT_DECK_COUNT, CardType.DEFAULT_DROP_CHANCE);
	}
}
