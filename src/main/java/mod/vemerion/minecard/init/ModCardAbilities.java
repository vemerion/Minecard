package mod.vemerion.minecard.init;

import java.util.function.Supplier;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.ability.AnimationAbility;
import mod.vemerion.minecard.game.ability.CardAbility;
import mod.vemerion.minecard.game.ability.CardAbilityType;
import mod.vemerion.minecard.game.ability.ChainAbility;
import mod.vemerion.minecard.game.ability.ChanceAbility;
import mod.vemerion.minecard.game.ability.ChoiceCardAbility;
import mod.vemerion.minecard.game.ability.ConstantCardsAbility;
import mod.vemerion.minecard.game.ability.DrawCardsAbility;
import mod.vemerion.minecard.game.ability.GameOverAbility;
import mod.vemerion.minecard.game.ability.ModifyAbility;
import mod.vemerion.minecard.game.ability.MoveCollectedAbility;
import mod.vemerion.minecard.game.ability.MultiAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import mod.vemerion.minecard.game.ability.PlaceCardsAbility;
import mod.vemerion.minecard.game.ability.RemoveCardsAbility;
import mod.vemerion.minecard.game.ability.ResourceAbility;
import mod.vemerion.minecard.game.ability.SelectCardsAbility;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ModCardAbilities {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Main.MODID, "card_abilities");

	public static final DeferredRegister<CardAbilityType<?>> CARD_ABILITIES = DeferredRegister.create(REGISTRY_NAME,
			Main.MODID);

	@SuppressWarnings("unchecked")
	private static Supplier<IForgeRegistry<CardAbilityType<?>>> supplier = CARD_ABILITIES.makeRegistry(
			(Class<CardAbilityType<?>>) (Class<?>) CardAbilityType.class,
			() -> new RegistryBuilder<CardAbilityType<?>>().setName(REGISTRY_NAME));

	public static IForgeRegistry<CardAbilityType<?>> getRegistry() {
		return supplier.get();
	}

	public static final RegistryObject<CardAbilityType<? extends CardAbility>> NO_CARD_ABILITY = CARD_ABILITIES
			.register("no_card_ability", () -> new CardAbilityType<NoCardAbility>(NoCardAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> DRAW_CARDS = CARD_ABILITIES
			.register("draw_cards", () -> new CardAbilityType<DrawCardsAbility>(DrawCardsAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> MODIFY = CARD_ABILITIES
			.register("modify", () -> new CardAbilityType<ModifyAbility>(ModifyAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> RESOURCE = CARD_ABILITIES
			.register("resource", () -> new CardAbilityType<ResourceAbility>(ResourceAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> MULTI = CARD_ABILITIES.register("multi",
			() -> new CardAbilityType<MultiAbility>(MultiAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> CHANCE = CARD_ABILITIES
			.register("chance", () -> new CardAbilityType<ChanceAbility>(ChanceAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> CHOICE = CARD_ABILITIES
			.register("choice", () -> new CardAbilityType<ChoiceCardAbility>(ChoiceCardAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> GAME_OVER = CARD_ABILITIES
			.register("game_over", () -> new CardAbilityType<GameOverAbility>(GameOverAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> SELECT_CARDS = CARD_ABILITIES
			.register("select_cards", () -> new CardAbilityType<SelectCardsAbility>(SelectCardsAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> CHAIN = CARD_ABILITIES.register("chain",
			() -> new CardAbilityType<ChainAbility>(ChainAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> CONSTANT_CARDS = CARD_ABILITIES
			.register("constant_cards", () -> new CardAbilityType<ConstantCardsAbility>(ConstantCardsAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> REMOVE_CARDS = CARD_ABILITIES
			.register("remove_cards", () -> new CardAbilityType<RemoveCardsAbility>(RemoveCardsAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> PLACE_CARDS = CARD_ABILITIES
			.register("place_cards", () -> new CardAbilityType<PlaceCardsAbility>(PlaceCardsAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> ANIMATION = CARD_ABILITIES
			.register("animation", () -> new CardAbilityType<AnimationAbility>(AnimationAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> MOVE_COLLECTED = CARD_ABILITIES
			.register("move_collected", () -> new CardAbilityType<MoveCollectedAbility>(MoveCollectedAbility.CODEC));

}
