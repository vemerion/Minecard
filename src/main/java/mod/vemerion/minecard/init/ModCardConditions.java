package mod.vemerion.minecard.init;

import java.util.function.Supplier;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.ability.CardCondition;
import mod.vemerion.minecard.game.ability.CardCondition.CardConditionType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ModCardConditions {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Main.MODID, "card_conditions");

	public static final DeferredRegister<CardConditionType<?>> CARD_CONDITIONS = DeferredRegister.create(REGISTRY_NAME,
			Main.MODID);

	@SuppressWarnings("unchecked")
	private static Supplier<IForgeRegistry<CardConditionType<?>>> supplier = CARD_CONDITIONS.makeRegistry(
			(Class<CardConditionType<?>>) (Class<?>) CardConditionType.class,
			() -> new RegistryBuilder<CardConditionType<?>>().setName(REGISTRY_NAME));

	public static IForgeRegistry<CardConditionType<?>> getRegistry() {
		return supplier.get();
	}

	public static final RegistryObject<CardConditionType<? extends CardCondition>> NO_CONDITION = CARD_CONDITIONS
			.register("no_condition",
					() -> new CardConditionType<CardCondition.NoCondition>(CardCondition.NoCondition.CODEC));
	public static final RegistryObject<CardConditionType<? extends CardCondition>> AND = CARD_CONDITIONS.register("and",
			() -> new CardConditionType<CardCondition.And>(CardCondition.And.CODEC));
	public static final RegistryObject<CardConditionType<? extends CardCondition>> OR = CARD_CONDITIONS.register("or",
			() -> new CardConditionType<CardCondition.Or>(CardCondition.Or.CODEC));
	public static final RegistryObject<CardConditionType<? extends CardCondition>> NOT = CARD_CONDITIONS.register("not",
			() -> new CardConditionType<CardCondition.Not>(CardCondition.Not.CODEC));
	public static final RegistryObject<CardConditionType<? extends CardCondition>> ENTITY = CARD_CONDITIONS
			.register("entity", () -> new CardConditionType<CardCondition.Entity>(CardCondition.Entity.CODEC));
	public static final RegistryObject<CardConditionType<? extends CardCondition>> OPERATOR = CARD_CONDITIONS.register(
			"operator",
			() -> new CardConditionType<CardCondition.OperatorCondition>(CardCondition.OperatorCondition.CODEC));

}
