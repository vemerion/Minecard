package mod.vemerion.minecard.init;

import java.util.function.Supplier;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.screen.animation.config.AnimationConfig;
import mod.vemerion.minecard.screen.animation.config.AnimationConfigType;
import mod.vemerion.minecard.screen.animation.config.EvokerFangsAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.GlowAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.NoAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.ThrowItemAnimationConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

public class ModAnimationConfigs {
	private static final ResourceLocation REGISTRY_NAME = new ResourceLocation(Main.MODID, "animation_configs");

	public static final DeferredRegister<AnimationConfigType<?>> ANIMATION_CONFIGS = DeferredRegister
			.create(REGISTRY_NAME, Main.MODID);

	@SuppressWarnings("unchecked")
	private static Supplier<IForgeRegistry<AnimationConfigType<? extends AnimationConfig>>> supplier = ANIMATION_CONFIGS
			.makeRegistry((Class<AnimationConfigType<? extends AnimationConfig>>) (Class<?>) AnimationConfigType.class,
					() -> new RegistryBuilder<AnimationConfigType<? extends AnimationConfig>>().setName(REGISTRY_NAME));

	public static IForgeRegistry<AnimationConfigType<? extends AnimationConfig>> getRegistry() {
		return supplier.get();
	}

	public static final RegistryObject<AnimationConfigType<? extends AnimationConfig>> NO_ANIMATION_CONFIG = ANIMATION_CONFIGS
			.register("no_animation_config", () -> new AnimationConfigType<NoAnimationConfig>(NoAnimationConfig.CODEC));
	public static final RegistryObject<AnimationConfigType<? extends AnimationConfig>> THROW_ITEM = ANIMATION_CONFIGS
			.register("throw_item",
					() -> new AnimationConfigType<ThrowItemAnimationConfig>(ThrowItemAnimationConfig.CODEC));
	public static final RegistryObject<AnimationConfigType<? extends AnimationConfig>> GLOW = ANIMATION_CONFIGS
			.register("glow", () -> new AnimationConfigType<GlowAnimationConfig>(GlowAnimationConfig.CODEC));
	public static final RegistryObject<AnimationConfigType<? extends AnimationConfig>> EVOKER_FANGS = ANIMATION_CONFIGS
			.register(
					"evoker_fangs", () -> new AnimationConfigType<EvokerFangsAnimationConfig>(EvokerFangsAnimationConfig.CODEC));
}
