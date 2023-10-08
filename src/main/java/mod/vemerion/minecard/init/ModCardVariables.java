package mod.vemerion.minecard.init;

import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.ability.CardVariable;
import mod.vemerion.minecard.game.ability.CardVariable.CardVariableType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ModCardVariables {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Main.MODID, "card_variables");

	public static final DeferredRegister<CardVariableType<?>> CARD_VARIABLES = DeferredRegister.create(REGISTRY_NAME,
			Main.MODID);

	private static Supplier<IForgeRegistry<CardVariableType<?>>> supplier = CARD_VARIABLES
			.makeRegistry(() -> new RegistryBuilder<CardVariableType<?>>().setName(REGISTRY_NAME));

	public static IForgeRegistry<CardVariableType<?>> getRegistry() {
		return supplier.get();
	}

	public static final RegistryObject<CardVariableType<? extends CardVariable>> COST = CARD_VARIABLES.register("cost",
			() -> new CardVariableType<CardVariable.SimpleVariable>(Codec.unit(CardVariable.COST)));
	public static final RegistryObject<CardVariableType<? extends CardVariable>> DAMAGE = CARD_VARIABLES.register(
			"damage", () -> new CardVariableType<CardVariable.SimpleVariable>(Codec.unit(CardVariable.DAMAGE)));
	public static final RegistryObject<CardVariableType<? extends CardVariable>> MAX_HEALTH = CARD_VARIABLES.register(
			"max_health", () -> new CardVariableType<CardVariable.SimpleVariable>(Codec.unit(CardVariable.MAX_HEALTH)));
	public static final RegistryObject<CardVariableType<? extends CardVariable>> HEALTH = CARD_VARIABLES.register(
			"health", () -> new CardVariableType<CardVariable.SimpleVariable>(Codec.unit(CardVariable.HEALTH)));
	public static final RegistryObject<CardVariableType<? extends CardVariable>> PROPERTY = CARD_VARIABLES.register(
			"property", () -> new CardVariableType<CardVariable.PropertyVariable>(CardVariable.PropertyVariable.CODEC));

}
