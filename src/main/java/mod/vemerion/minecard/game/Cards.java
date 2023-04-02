package mod.vemerion.minecard.game;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.UpdateCardTypesMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;

public class Cards extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	public static final String FOLDER_NAME = Main.MODID + "_cards";

	public static final CardType EMPTY_CARD_TYPE = new CardType(null, 0, 0, 0, AdditionalCardData.EMPTY);
	public static final Card EMPTY_CARD = EMPTY_CARD_TYPE.create();
	public static final CardType PLAYER = new CardType(EntityType.PLAYER, 0, 30, 0, AdditionalCardData.EMPTY);

	private static Cards clientInstance;
	private static Cards serverInstance;

	private final Map<ResourceLocation, CardType> CARDS;

	private Cards() {
		super(GSON, FOLDER_NAME);
		CARDS = new HashMap<>();
	}

	public CardType get(EntityType<?> type) {
		return CARDS.computeIfAbsent(type.getRegistryName(), rl -> generateCardType(type));
	}

	public static Cards getInstance(boolean isClient) {
		if (clientInstance == null)
			clientInstance = new Cards();
		if (serverInstance == null)
			serverInstance = new Cards();
		return isClient ? clientInstance : serverInstance;
	}

	public static Cards getInstance(Level level) {
		return getInstance(level.isClientSide);
	}

	public static boolean isAllowed(EntityType<?> type) {
		return type.getCategory() != MobCategory.MISC;
	}

	private CardType generateCardType(EntityType<?> type) {
		Random rand = new Random(type.getRegistryName().toString().hashCode());

		int cost = rand.nextInt(1, 11);
		int totalStats = cost * 2 + 1;
		int health = rand.nextInt(1, totalStats);

		return new CardType(type, cost, health, totalStats - health, AdditionalCardData.EMPTY);
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager,
			ProfilerFiller pProfiler) {
		Map<ResourceLocation, CardType> newCardTypes = new HashMap<>();
		for (var entry : pObject.entrySet()) {
			var key = entry.getKey();

			var json = GsonHelper.convertToJsonObject(entry.getValue(), "top element");
			var cardType = CardType.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow(false, s -> {
				Main.LOGGER.error("Could not decode card '" + key + "': " + s);
			});

			newCardTypes.put(key, cardType);
		}

		CARDS.putAll(newCardTypes);
		if (ServerLifecycleHooks.getCurrentServer() != null)
			sendCardTypeMessage(newCardTypes);
	}

	private void sendCardTypeMessage(Map<ResourceLocation, CardType> cardTypes) {
		Network.INSTANCE.send(PacketDistributor.ALL.noArg(), new UpdateCardTypesMessage(cardTypes));
	}

	public void sendAllCardTypeMessage(ServerPlayer reciever) {
		Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> reciever), new UpdateCardTypesMessage(CARDS));
	}

	public void addCardTypes(Map<ResourceLocation, CardType> cardTypes) {
		CARDS.putAll(cardTypes);
	}
}
