package mod.vemerion.minecard.init;

import java.util.function.Supplier;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.ability.CardAbility;
import mod.vemerion.minecard.game.ability.CardAbilityType;
import mod.vemerion.minecard.game.ability.DrawCardsAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
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
}
