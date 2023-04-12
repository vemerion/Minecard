package mod.vemerion.minecard.screen.animation.config;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.Main;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

public class AnimationConfigs extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	public static final String FOLDER_NAME = Main.MODID + "_animations";

	private static AnimationConfigs instance;

	private final Map<ResourceLocation, AnimationConfig> ANIMATIONS;

	private AnimationConfigs() {
		super(GSON, FOLDER_NAME);
		ANIMATIONS = new HashMap<>();
	}

	public AnimationConfig get(ResourceLocation rl) {
		return ANIMATIONS.get(rl);
	}

	public static AnimationConfigs getInstance() {
		if (instance == null)
			instance = new AnimationConfigs();
		return instance;
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager,
			ProfilerFiller pProfiler) {
		Map<ResourceLocation, AnimationConfig> newAnimations = new HashMap<>();
		for (var entry : pObject.entrySet()) {
			var file = entry.getKey();

			var json = GsonHelper.convertToJsonObject(entry.getValue(), "top element");
			for (var animEntry : json.entrySet()) {
				var key = new ResourceLocation(file.getNamespace(), animEntry.getKey());
				var config = AnimationConfig.CODEC.parse(JsonOps.INSTANCE, animEntry.getValue()).getOrThrow(false, s -> {
					Main.LOGGER.error("Could not decode animation '" + animEntry.getKey() + "' in " + file + ": " + s);
				});
				newAnimations.put(key, config);
			}
		}

		ANIMATIONS.putAll(newAnimations);
	}
}
