package mod.vemerion.minecard.datagen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.screen.animation.config.AnimationConfig;
import mod.vemerion.minecard.screen.animation.config.AnimationConfigs;
import mod.vemerion.minecard.screen.animation.config.ChargeAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.EvokerFangsAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.ExplosionAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.GlowAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.LightningBoltAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.PotionAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.ThrowItemAnimationConfig;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

public class ModAnimationConfigProvider implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final String FILE_NAME = "animations.json";

	private Map<String, AnimationConfig> animations = new HashMap<>();
	private DataGenerator generator;

	public ModAnimationConfigProvider(DataGenerator generator) {
		this.generator = generator;
	}

	@Override
	public void run(HashCache cache) throws IOException {
		addAnimations();
		var folder = generator.getOutputFolder();
		var json = new JsonObject();
		for (var entry : animations.entrySet()) {
			json.add(entry.getKey(),
					AnimationConfig.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, s -> {
					}));
		}
		var path = folder.resolve("assets").resolve(Main.MODID).resolve(AnimationConfigs.FOLDER_NAME)
				.resolve(FILE_NAME);
		try {
			DataProvider.save(GSON, cache, json, path);
		} catch (IOException e) {
			Main.LOGGER.error("Couldn't save animations " + path + ": " + e);
		}
	}

	private void addAnimations() {
		animations.put("throw_carrot", new ThrowItemAnimationConfig(Items.CARROT, Optional.empty()));
		animations.put("glow", GlowAnimationConfig.INSTANCE);
		animations.put("evoker_fangs", EvokerFangsAnimationConfig.INSTANCE);
		animations.put("ender_dragon", new PotionAnimationConfig(0.8f, 0, 0.9f));
		animations.put("target_explosion", new ExplosionAnimationConfig(true, false));
		animations.put("origin_explosion", new ExplosionAnimationConfig(false, true));
		animations.put("wither_projectile", new ThrowItemAnimationConfig(Items.WITHER_SKELETON_SKULL,
				Optional.of(new ResourceLocation(Main.MODID, "target_explosion"))));
		animations.put("throw_pork", new ThrowItemAnimationConfig(Items.PORKCHOP, Optional.empty()));
		animations.put("fireball", new ThrowItemAnimationConfig(Items.FIRE_CHARGE, Optional.empty()));
		animations.put("throw_bamboo", new ThrowItemAnimationConfig(Items.BAMBOO, Optional.empty()));
		animations.put("elder_guardian", new PotionAnimationConfig(0.29f, 0.26f, 0.09f));
		animations.put("goat_charge", new ChargeAnimationConfig(EntityType.GOAT, 25, 25,
				Optional.of(SoundEvents.HORSE_GALLOP), Optional.of(SoundEvents.GOAT_RAM_IMPACT)));
		animations.put("throw_shield", new ThrowItemAnimationConfig(Items.SHIELD, Optional.empty()));
		animations.put("shoot_arrow", new ChargeAnimationConfig(EntityType.ARROW, 12, 25, Optional.empty(),
				Optional.of(SoundEvents.ARROW_HIT)));
		animations.put("ravager_charge", new ChargeAnimationConfig(EntityType.RAVAGER, 25, 15,
				Optional.of(SoundEvents.RAVAGER_STEP), Optional.of(SoundEvents.RAVAGER_ATTACK)));
		animations.put("throw_web", new ThrowItemAnimationConfig(Items.COBWEB, Optional.empty()));
		animations.put("lightning_bolt", LightningBoltAnimationConfig.INSTANCE);

	}

	@Override
	public String getName() {
		return Main.MODID + ": Animation Configs";
	}

}
