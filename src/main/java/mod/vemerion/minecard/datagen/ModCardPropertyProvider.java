package mod.vemerion.minecard.datagen;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.CardProperties;
import mod.vemerion.minecard.game.CardProperty;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ModCardPropertyProvider implements DataProvider {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	private Map<ResourceLocation, CardProperty> properties = new HashMap<>();
	private DataGenerator generator;

	public ModCardPropertyProvider(DataGenerator generator) {
		this.generator = generator;
	}

	@Override
	public void run(HashCache cache) throws IOException {
		addProperties();
		var folder = generator.getOutputFolder();
		for (var entry : properties.entrySet()) {
			var key = entry.getKey();
			var path = folder.resolve(
					"data/" + key.getNamespace() + "/" + CardProperties.FOLDER_NAME + "/" + key.getPath() + ".json");
			try {
				DataProvider.save(GSON, cache,
						CardProperty.CODEC.encodeStart(JsonOps.INSTANCE, entry.getValue()).getOrThrow(false, s -> {
						}), path);
			} catch (IOException e) {
				Main.LOGGER.error("Couldn't save card property " + path + ": " + e);
			}
		}
	}

	private void addProperties() {
		properties.put(CardProperty.TAUNT, new CardProperty(new ItemStack(Items.CARROT_ON_A_STICK)));
		properties.put(CardProperty.CHARGE, new CardProperty(new ItemStack(Items.SUGAR)));
		properties.put(CardProperty.STEALTH, new CardProperty(new ItemStack(Items.TALL_GRASS)));
		properties.put(CardProperty.FREEZE, new CardProperty(new ItemStack(Items.ICE)));
		properties.put(CardProperty.SHIELD, new CardProperty(new ItemStack(Items.DIAMOND_CHESTPLATE)));
		properties.put(CardProperty.BURN, new CardProperty(new ItemStack(Items.LAVA_BUCKET)));
		properties.put(CardProperty.SPECIAL, new CardProperty(new ItemStack(Items.ENCHANTED_GOLDEN_APPLE)));
		properties.put(CardProperty.BABY, new CardProperty(new ItemStack(Items.EGG)));
		properties.put(CardProperty.THORNS, new CardProperty(new ItemStack(Items.POINTED_DRIPSTONE)));
		properties.put(CardProperty.POISON, new CardProperty(new ItemStack(Items.SPIDER_EYE)));
	}

	@Override
	public String getName() {
		return Main.MODID + ": Card Properties";
	}

}
