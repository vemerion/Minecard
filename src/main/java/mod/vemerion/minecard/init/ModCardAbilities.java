package mod.vemerion.minecard.init;

import java.util.function.Supplier;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.ability.AddCardsAbility;
import mod.vemerion.minecard.game.ability.CardAbility;
import mod.vemerion.minecard.game.ability.CardAbilityType;
import mod.vemerion.minecard.game.ability.CopyCardsAbility;
import mod.vemerion.minecard.game.ability.DrawCardsAbility;
import mod.vemerion.minecard.game.ability.ModifyAbility;
import mod.vemerion.minecard.game.ability.MultiAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import mod.vemerion.minecard.game.ability.ResourceAbility;
import mod.vemerion.minecard.game.ability.SummonCardAbility;
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
	private static Supplier<IForgeRegistry<CardAbilityType<? extends CardAbility>>> supplier = CARD_ABILITIES
			.makeRegistry((Class<CardAbilityType<? extends CardAbility>>) (Class<?>) CardAbilityType.class,
					() -> new RegistryBuilder<CardAbilityType<? extends CardAbility>>().setName(REGISTRY_NAME));

	public static IForgeRegistry<CardAbilityType<? extends CardAbility>> getRegistry() {
		return supplier.get();
	}

	public static final RegistryObject<CardAbilityType<? extends CardAbility>> NO_CARD_ABILITY = CARD_ABILITIES
			.register("no_card_ability", () -> new CardAbilityType<NoCardAbility>(NoCardAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> DRAW_CARDS = CARD_ABILITIES
			.register("draw_cards", () -> new CardAbilityType<DrawCardsAbility>(DrawCardsAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> MODIFY = CARD_ABILITIES
			.register("modify", () -> new CardAbilityType<ModifyAbility>(ModifyAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> ADD_CARDS = CARD_ABILITIES
			.register("add_cards", () -> new CardAbilityType<AddCardsAbility>(AddCardsAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> RESOURCE = CARD_ABILITIES
			.register("resource", () -> new CardAbilityType<ResourceAbility>(ResourceAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> COPY_CARDS = CARD_ABILITIES
			.register("copy_cards", () -> new CardAbilityType<CopyCardsAbility>(CopyCardsAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> SUMMON_CARD = CARD_ABILITIES
			.register("summon_card", () -> new CardAbilityType<SummonCardAbility>(SummonCardAbility.CODEC));
	public static final RegistryObject<CardAbilityType<? extends CardAbility>> MULTI = CARD_ABILITIES.register("multi",
			() -> new CardAbilityType<MultiAbility>(MultiAbility.CODEC));

}
