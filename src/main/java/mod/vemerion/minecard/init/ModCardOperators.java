package mod.vemerion.minecard.init;

import java.util.function.Supplier;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.ability.CardOperator;
import mod.vemerion.minecard.game.ability.CardOperator.CardOperatorType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ModCardOperators {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Main.MODID, "card_operators");

	public static final DeferredRegister<CardOperatorType<?>> CARD_OPERATORS = DeferredRegister.create(REGISTRY_NAME,
			Main.MODID);

	@SuppressWarnings("unchecked")
	private static Supplier<IForgeRegistry<CardOperatorType<?>>> supplier = CARD_OPERATORS.makeRegistry(
			(Class<CardOperatorType<?>>) (Class<?>) CardOperatorType.class,
			() -> new RegistryBuilder<CardOperatorType<?>>().setName(REGISTRY_NAME));

	public static IForgeRegistry<CardOperatorType<?>> getRegistry() {
		return supplier.get();
	}

	public static final RegistryObject<CardOperatorType<? extends CardOperator>> VARIABLE = CARD_OPERATORS
			.register("variable", () -> new CardOperatorType<CardOperator.Variable>(CardOperator.Variable.CODEC));
	public static final RegistryObject<CardOperatorType<? extends CardOperator>> CONSTANT = CARD_OPERATORS
			.register("constant", () -> new CardOperatorType<CardOperator.Constant>(CardOperator.Constant.CODEC));
	public static final RegistryObject<CardOperatorType<? extends CardOperator>> RANDOM = CARD_OPERATORS.register(
			"random", () -> new CardOperatorType<CardOperator.RandomOperator>(CardOperator.RandomOperator.CODEC));
	public static final RegistryObject<CardOperatorType<? extends CardOperator>> ADD = CARD_OPERATORS.register("add",
			() -> new CardOperatorType<CardOperator.Add>(CardOperator.Add.CODEC));
	public static final RegistryObject<CardOperatorType<? extends CardOperator>> SUB = CARD_OPERATORS.register("sub",
			() -> new CardOperatorType<CardOperator.Sub>(CardOperator.Sub.CODEC));
	public static final RegistryObject<CardOperatorType<? extends CardOperator>> MUL = CARD_OPERATORS.register("mul",
			() -> new CardOperatorType<CardOperator.Mul>(CardOperator.Mul.CODEC));

}
