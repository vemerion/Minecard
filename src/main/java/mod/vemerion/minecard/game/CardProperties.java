package mod.vemerion.minecard.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.ability.NoCardAbility;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.UpdateCardPropertiesMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

public class CardProperties extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	public static final String FOLDER_NAME = Main.MODID + "_cards_properties";

	private static final CardProperty NO_CARD_PROPERTY = new CardProperty(ItemStack.EMPTY,
			NoCardAbility.NO_CARD_ABILITY);

	private static CardProperties clientInstance;
	private static CardProperties serverInstance;

	private final Map<ResourceLocation, CardProperty> PROPERTIES;

	private CardProperties() {
		super(GSON, FOLDER_NAME);
		PROPERTIES = new HashMap<>();
	}

	public CardProperty get(ResourceLocation rl) {
		return PROPERTIES.getOrDefault(rl, NO_CARD_PROPERTY);
	}

	public Set<Map.Entry<ResourceLocation, CardProperty>> entries() {
		return PROPERTIES.entrySet();
	}

	public ResourceLocation randomKey(Random rand) {
		var keys = PROPERTIES.keySet();
		int i = rand.nextInt(keys.size());
		for (var key : keys)
			if (i-- == 0)
				return key;
		return null;
	}

	public static CardProperties getInstance(boolean isClient) {
		if (clientInstance == null)
			clientInstance = new CardProperties();
		if (serverInstance == null)
			serverInstance = new CardProperties();
		return isClient ? clientInstance : serverInstance;
	}

	public static CardProperties getInstance(Level level) {
		return getInstance(level.isClientSide);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager,
			ProfilerFiller pProfiler) {
		Map<ResourceLocation, CardProperty> properties = new HashMap<>();
		for (var entry : pObject.entrySet()) {
			var key = entry.getKey();

			var json = GsonHelper.convertToJsonObject(entry.getValue(), "top element");
			var property = CardProperty.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, s -> {
				Main.LOGGER.error("Could not decode card property '" + key + "': " + s);
			});

			properties.put(key, property);
		}

		PROPERTIES.putAll(properties);
		if (ServerLifecycleHooks.getCurrentServer() != null)
			sendCardPropertiesMessage(properties);
	}

	private void sendCardPropertiesMessage(Map<ResourceLocation, CardProperty> cardProperties) {
		Network.INSTANCE.send(PacketDistributor.ALL.noArg(), new UpdateCardPropertiesMessage(cardProperties));
	}

	public void sendAllCardPropertiesMessage(ServerPlayer reciever) {
		Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> reciever),
				new UpdateCardPropertiesMessage(PROPERTIES));
	}

	public void addProperties(Map<ResourceLocation, CardProperty> cardProperties) {
		PROPERTIES.putAll(cardProperties);
	}
}
