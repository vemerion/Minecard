package mod.vemerion.minecard.init;

import java.util.function.Supplier;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.ability.CardSelectionMethod;
import mod.vemerion.minecard.game.ability.CardSelectionMethod.CardSelectionMethodType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ModCardSelectionMethods {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Main.MODID, "card_selection_methods");

	public static final DeferredRegister<CardSelectionMethodType<?>> CARD_SELECTION_METHODS = DeferredRegister
			.create(REGISTRY_NAME, Main.MODID);

	@SuppressWarnings("unchecked")
	private static Supplier<IForgeRegistry<CardSelectionMethodType<?>>> supplier = CARD_SELECTION_METHODS.makeRegistry(
			(Class<CardSelectionMethodType<?>>) (Class<?>) CardSelectionMethodType.class,
			() -> new RegistryBuilder<CardSelectionMethodType<?>>().setName(REGISTRY_NAME));

	public static IForgeRegistry<CardSelectionMethodType<?>> getRegistry() {
		return supplier.get();
	}

	public static final RegistryObject<CardSelectionMethodType<? extends CardSelectionMethod>> ALL = CARD_SELECTION_METHODS
			.register("all", () -> new CardSelectionMethodType<CardSelectionMethod.All>(CardSelectionMethod.All.CODEC));
	public static final RegistryObject<CardSelectionMethodType<? extends CardSelectionMethod>> RANDOM = CARD_SELECTION_METHODS
			.register("random",
					() -> new CardSelectionMethodType<CardSelectionMethod.Random>(CardSelectionMethod.Random.CODEC));
	public static final RegistryObject<CardSelectionMethodType<? extends CardSelectionMethod>> CHOICE = CARD_SELECTION_METHODS
			.register("choice",
					() -> new CardSelectionMethodType<CardSelectionMethod.Choice>(CardSelectionMethod.Choice.CODEC));

}
