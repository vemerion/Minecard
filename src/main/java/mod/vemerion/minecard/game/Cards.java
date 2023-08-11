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
import mod.vemerion.minecard.game.ability.CardAbilityTrigger;
import mod.vemerion.minecard.game.ability.DrawCardsAbility;
import mod.vemerion.minecard.game.ability.NoCardAbility;
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
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;

public class Cards extends SimpleJsonResourceReloadListener {
	private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
	public static final String FOLDER_NAME = Main.MODID + "_cards";

	public static final CardType EMPTY_CARD_TYPE = new CardType(null, 0, 1, 1, Map.of(), NoCardAbility.NO_CARD_ABILITY,
			AdditionalCardData.EMPTY, 0, 0);
	public static final CardType TUTORIAL_CARD_TYPE = new CardType(EntityType.CREEPER, 1, 2, 1,
			Map.of(CardProperty.THORNS, 1), new DrawCardsAbility(Set.of(CardAbilityTrigger.ATTACK),
					"card_ability." + Main.MODID + ".tutorial_creeper", 2),
			AdditionalCardData.EMPTY, 0, 0);

	public static final Set<ResourceLocation> SPELLS = Set.of(new ResourceLocation(Main.MODID, "fishing_rod"),
			new ResourceLocation(Main.MODID, "book"), new ResourceLocation(Main.MODID, "splash_potion_of_harming"),
			new ResourceLocation(Main.MODID, "enchanted_golden_apple"), new ResourceLocation(Main.MODID, "chest"),
			new ResourceLocation(Main.MODID, "enchanted_book"), new ResourceLocation(Main.MODID, "spyglass"),
			new ResourceLocation(Main.MODID, "lodestone"), new ResourceLocation(Main.MODID, "soul_sand"),
			new ResourceLocation(Main.MODID, "amethyst_shard"), new ResourceLocation(Main.MODID, "wooden_sword"));

	private static Cards clientInstance;
	private static Cards serverInstance;

	private final Map<ResourceLocation, CardType> CARDS;

	private Cards() {
		super(GSON, FOLDER_NAME);
		CARDS = new HashMap<>();
	}

	public CardType get(ResourceLocation rl) {
		return CARDS.computeIfAbsent(rl, r -> generateCardType(ForgeRegistries.ENTITIES.getValue(r)));
	}

	public CardType get(EntityType<?> type) {
		return get(type.getRegistryName());
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
		return type.getCategory() != MobCategory.MISC || type == EntityType.VILLAGER || type == EntityType.IRON_GOLEM
				|| type == EntityType.SNOW_GOLEM;
	}

	private CardType generateCardType(EntityType<?> type) {
		if (type == null)
			return null;

		var rand = new Random(type.getRegistryName().toString().hashCode());

		int cost = rand.nextInt(1, 11);
		int totalStats = cost * 2 + 1;
		int health = rand.nextInt(1, totalStats);

		return new CardType(type, cost, health, totalStats - health, Map.of(), NoCardAbility.NO_CARD_ABILITY,
				AdditionalCardData.EMPTY, CardType.DEFAULT_DECK_COUNT, CardType.DEFAULT_DROP_CHANCE);
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
