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
import mod.vemerion.minecard.screen.animation.ParticlesAnimation;
import mod.vemerion.minecard.screen.animation.config.AnimationConfig;
import mod.vemerion.minecard.screen.animation.config.AnimationConfigs;
import mod.vemerion.minecard.screen.animation.config.EnderDragonAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.EntityAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.ExplosionAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.GlowAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.PotionAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.SplashAnimationConfig;
import mod.vemerion.minecard.screen.animation.config.ThrowItemAnimationConfig;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

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
		animations.put("glow", GlowAnimationConfig.INSTANCE);
		animations.put("ender_dragon", EnderDragonAnimationConfig.INSTANCE);
		animations.put("target_explosion", new ExplosionAnimationConfig(true, false));
		animations.put("origin_explosion", new ExplosionAnimationConfig(false, true));
		animations.put("wither_projectile",
				new ThrowItemAnimationConfig(Items.WITHER_SKELETON_SKULL, Optional.of(SoundEvents.WITHER_SHOOT),
						Optional.empty(), Optional.of(new ResourceLocation(Main.MODID, "target_explosion"))));
		animations.put("throw_pork", new ThrowItemAnimationConfig(Items.PORKCHOP, Optional.empty(),
				Optional.of(SoundEvents.PLAYER_BURP), Optional.empty()));
		animations.put("fireball",
				new ThrowItemAnimationConfig(Items.FIRE_CHARGE, Optional.of(SoundEvents.GHAST_SHOOT),
						Optional.of(SoundEvents.GENERIC_EXPLODE),
						Optional.of(new ResourceLocation(Main.MODID, "orange_splash"))));
		animations.put("orange_splash", new SplashAnimationConfig(new ParticlesAnimation.Color(1, 0.4f, 0)));
		animations.put("throw_bamboo", new ThrowItemAnimationConfig(Items.BAMBOO, Optional.empty(), Optional.empty(),
				Optional.of(new ResourceLocation(Main.MODID, "green_splash"))));
		animations.put("elder_guardian", new PotionAnimationConfig(new ParticlesAnimation.Color(0.29f, 0.26f, 0.09f)));
		animations.put("goat_charge", new EntityAnimationConfig(EntityType.GOAT, true, 25, 25, 0, Optional.empty(),
				Optional.of(SoundEvents.HORSE_GALLOP), Optional.of(SoundEvents.GOAT_RAM_IMPACT)));
		animations.put("throw_shield", new ThrowItemAnimationConfig(Items.SHIELD, Optional.empty(),
				Optional.of(SoundEvents.SHIELD_BLOCK), Optional.empty()));
		animations.put("shoot_arrow", new EntityAnimationConfig(EntityType.ARROW, true, 12, 25, 1,
				Optional.of(SoundEvents.ARROW_SHOOT), Optional.empty(), Optional.of(SoundEvents.ARROW_HIT)));
		animations.put("ravager_charge", new EntityAnimationConfig(EntityType.RAVAGER, true, 25, 15, 0,
				Optional.empty(), Optional.of(SoundEvents.RAVAGER_STEP), Optional.of(SoundEvents.RAVAGER_ATTACK)));
		animations.put("throw_web", new ThrowItemAnimationConfig(Items.COBWEB));
		animations.put("evoker_fangs", new EntityAnimationConfig(EntityType.EVOKER_FANGS, false, 30, 30, 10,
				Optional.of(SoundEvents.EVOKER_FANGS_ATTACK), Optional.empty(), Optional.empty()));
		animations.put("lightning_bolt", new EntityAnimationConfig(EntityType.LIGHTNING_BOLT, false, 30, 15, 1,
				Optional.of(SoundEvents.LIGHTNING_BOLT_IMPACT), Optional.empty(), Optional.empty()));
		animations.put("throw_snowball",
				new ThrowItemAnimationConfig(Items.SNOWBALL, Optional.of(SoundEvents.SNOWBALL_THROW),
						Optional.of(SoundEvents.GENERIC_HURT),
						Optional.of(new ResourceLocation(Main.MODID, "white_splash"))));
		animations.put("throw_egg", new ThrowItemAnimationConfig(Items.EGG, Optional.of(SoundEvents.EGG_THROW),
				Optional.of(SoundEvents.CHICKEN_EGG), Optional.of(new ResourceLocation(Main.MODID, "egg_splash"))));
		animations.put("egg_splash", new SplashAnimationConfig(new ParticlesAnimation.Color(0.88f, 0.81f, 0.61f)));
		animations.put("throw_scute",
				new ThrowItemAnimationConfig(Items.SCUTE, Optional.of(SoundEvents.TURTLE_LAY_EGG),
						Optional.of(SoundEvents.TURTLE_EGG_BREAK),
						Optional.of(new ResourceLocation(Main.MODID, "green_splash"))));
		animations.put("green_splash", new SplashAnimationConfig(new ParticlesAnimation.Color(0.37f, 0.54f, 0.14f)));
		animations.put("throw_trident",
				new ThrowItemAnimationConfig(Items.TRIDENT, Optional.of(SoundEvents.TRIDENT_THROW),
						Optional.of(SoundEvents.TRIDENT_HIT),
						Optional.of(new ResourceLocation(Main.MODID, "lightning_bolt"))));
		animations.put("throw_milk", new ThrowItemAnimationConfig(Items.MILK_BUCKET, Optional.empty(),
				Optional.of(SoundEvents.GENERIC_DRINK), Optional.of(new ResourceLocation(Main.MODID, "white_splash"))));
		animations.put("white_splash", new SplashAnimationConfig(new ParticlesAnimation.Color(1, 1, 1)));
		animations.put("throw_sweet_berries", new ThrowItemAnimationConfig(Items.SWEET_BERRIES, Optional.empty(),
				Optional.of(SoundEvents.PLAYER_BURP), Optional.empty()));
		animations.put("throw_mushroom_stew", new ThrowItemAnimationConfig(Items.MUSHROOM_STEW, Optional.empty(),
				Optional.of(SoundEvents.PLAYER_BURP), Optional.empty()));
		animations.put("llama_spit", new EntityAnimationConfig(EntityType.LLAMA_SPIT, true, 15, 25, 1,
				Optional.of(SoundEvents.LLAMA_SPIT), Optional.empty(), Optional.of(SoundEvents.GENERIC_HURT)));
		animations.put("throw_rotten_flesh", new ThrowItemAnimationConfig(Items.ROTTEN_FLESH, Optional.empty(),
				Optional.of(SoundEvents.PLAYER_BURP), Optional.empty()));
		animations.put("throw_rabbit_foot", new ThrowItemAnimationConfig(Items.RABBIT_FOOT));
		animations.put("absorption_potion",
				new PotionAnimationConfig(new ParticlesAnimation.Color(0.46f, 0.36f, 0.38f)));
		animations.put("poison_potion", new PotionAnimationConfig(new ParticlesAnimation.Color(0.31f, 0.58f, 0.19f)));
		animations.put("healing_potion", new PotionAnimationConfig(new ParticlesAnimation.Color(0.97f, 0.14f, 0.14f)));
		animations.put("throw_absorption_potion",
				new ThrowItemAnimationConfig(
						PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.TURTLE_MASTER),
						Optional.of(SoundEvents.SPLASH_POTION_THROW), Optional.of(SoundEvents.SPLASH_POTION_BREAK),
						Optional.of(new ResourceLocation(Main.MODID, "absorption_potion"))));
		animations.put("throw_poison_potion",
				new ThrowItemAnimationConfig(PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.POISON),
						Optional.of(SoundEvents.SPLASH_POTION_THROW), Optional.of(SoundEvents.SPLASH_POTION_BREAK),
						Optional.of(new ResourceLocation(Main.MODID, "poison_potion"))));
		animations.put("throw_healing_potion",
				new ThrowItemAnimationConfig(PotionUtils.setPotion(Items.POTION.getDefaultInstance(), Potions.HEALING),
						Optional.of(SoundEvents.SPLASH_POTION_THROW), Optional.of(SoundEvents.SPLASH_POTION_BREAK),
						Optional.of(new ResourceLocation(Main.MODID, "healing_potion"))));
		animations.put("throw_packed_ice", new ThrowItemAnimationConfig(Items.PACKED_ICE, Optional.empty(),
				Optional.of(SoundEvents.PLAYER_HURT_FREEZE), Optional.empty()));
		animations.put("throw_pointed_dripstone", new ThrowItemAnimationConfig(Items.POINTED_DRIPSTONE,
				Optional.empty(), Optional.of(SoundEvents.DRIPSTONE_BLOCK_FALL), Optional.empty()));
		animations.put("throw_leather_chestplate", new ThrowItemAnimationConfig(Items.LEATHER_CHESTPLATE,
				Optional.empty(), Optional.of(SoundEvents.ARMOR_EQUIP_LEATHER), Optional.empty()));
		animations.put("throw_iron_sword", new ThrowItemAnimationConfig(Items.IRON_SWORD, Optional.empty(),
				Optional.of(SoundEvents.ARMOR_EQUIP_IRON), Optional.empty()));
		animations.put("throw_iron_boots", new ThrowItemAnimationConfig(Items.IRON_BOOTS, Optional.empty(),
				Optional.of(SoundEvents.ARMOR_EQUIP_IRON), Optional.empty()));
		animations.put("throw_ender_pearl", new ThrowItemAnimationConfig(Items.ENDER_PEARL,
				Optional.of(SoundEvents.ENDER_PEARL_THROW), Optional.empty(), Optional.empty()));

	}

	@Override
	public String getName() {
		return Main.MODID + ": Animation Configs";
	}

}
