package mod.vemerion.minecard.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.capability.DeckData;
import mod.vemerion.minecard.game.AIPlayer;
import mod.vemerion.minecard.game.AdditionalCardData;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.GameState;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.game.Receiver;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.init.ModBlockEntities;
import mod.vemerion.minecard.init.ModItems;
import mod.vemerion.minecard.network.GameOverMessage;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.NewTurnMessage;
import mod.vemerion.minecard.network.OpenGameMessage;
import mod.vemerion.minecard.network.SetReadyMessage;
import mod.vemerion.minecard.network.SetResourcesMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

public class GameBlockEntity extends BlockEntity {

	private static final int START_HAND_SIZE = 5;

	private GameState state;
	private Set<UUID> receivers;
	private AIPlayer aiPlayer;

	public GameBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(ModBlockEntities.GAME.get(), pWorldPosition, pBlockState);
		receivers = new HashSet<>();
		state = new GameState();
	}

	public void tick() {
		if (aiPlayer != null) {
			aiPlayer.tick();
		}
	}

	public boolean canReceiveMessage(ServerPlayer player) {
		return state.getPlayerStates().size() > 1;
	}

	public boolean isPlayerTurn(ServerPlayer player) {
		return state.getCurrentPlayer().equals(player.getUUID());
	}

	public void endTurn() {
		state.endTurn(getReceivers());
		var current = state.getCurrentPlayerState();
		for (var receiver : getReceivers()) {
			receiver.receiver(new NewTurnMessage(state.getCurrentPlayer()));
			receiver.receiver(new SetResourcesMessage(state.getCurrentPlayer(), current.getResources(),
					current.getMaxResources()));
			if (!current.getBoard().isEmpty()) {
				receiver.receiver(new SetReadyMessage(current.getId(), current.getReady()));
			}
		}

	}

	public void playCard(int card, int leftId) {
		state.getCurrentPlayerState().playCard(getReceivers(), card, leftId);
	}

	public void attack(int attacker, int target) {
		state.attack(getReceivers(), attacker, target);

		if (state.isGameOver()) {
			for (var receiver : getReceivers()) {
				receiver.receiver(new GameOverMessage());
			}

			state = new GameState();
			aiPlayer = null;
		}
	}

	public void closeGame(ServerPlayer sender) {
		receivers.remove(sender.getUUID());
	}

	private List<Receiver> getReceivers() {
		var list = new ArrayList<Receiver>();
		for (var id : receivers) {
			if (id.equals(AIPlayer.ID)) {
				list.add(new Receiver.AI(aiPlayer));
			} else {
				var player = level.getPlayerByUUID(id);
				if (player != null) {
					list.add(new Receiver.Player((ServerPlayer) player));
				}
			}
		}
		return list;
	}

	public void open(ServerPlayer player, ItemStack stack) {
		var id = player.getUUID();
		if (state.getPlayerStates().size() > 1) {
			receivers.add(player.getUUID());
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), createOpenGameMessage(id));
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
					new NewTurnMessage(state.getCurrentPlayer()));
		} else if (stack.is(ModItems.DECK.get())
				&& state.getPlayerStates().stream().noneMatch(s -> s.getId().equals(player.getUUID()))) {
			addRealPlayer(player, stack);
		} else if (stack.is(Items.REDSTONE)
				&& state.getPlayerStates().stream().noneMatch(s -> s.getId().equals(AIPlayer.ID))) {
			addAIPlayer();
		} else {
			player.sendMessage(new TranslatableComponent(Helper.chat("game_interactions")), id);
		}
	}

	private void addPlayer(UUID id, List<Card> deck) {
		Collections.shuffle(deck);
		List<Card> hand = new ArrayList<>();
		for (int i = 0; i < START_HAND_SIZE; i++) {
			hand.add(deck.remove(deck.size() - 1));
		}

		List<Card> board = new ArrayList<>();
		var playerCard = Cards.getInstance(false).get(EntityType.PLAYER).create();
		playerCard.setAdditionalData(new AdditionalCardData.IdData(id));
		board.add(playerCard);

		var playerState = new PlayerState(id, deck, hand, board, 1, 1);
		playerState.setGame(state);
		state.getPlayerStates().add(playerState);

		if (state.getPlayerStates().size() > 1 && aiPlayer != null) {
			var receiver = new Receiver.AI(aiPlayer);
			receiver.receiver(createOpenGameMessage(receiver.getId()));
			receiver.receiver(new NewTurnMessage(state.getCurrentPlayer()));
		}
	}

	private void addAIPlayer() {
		List<Card> deck = new ArrayList<>();

		var entities = ForgeRegistries.ENTITIES.getValues();

		while (deck.size() < DeckData.CAPACITY) {
			int i = level.random.nextInt(entities.size());
			for (var entity : entities) {
				if (i == 0) {
					if (Cards.isAllowed(entity)) {
						deck.add(Cards.getInstance(false).get(entity).create());
					}
					break;
				}
				i--;
			}
		}

		aiPlayer = new AIPlayer(this);
		receivers.add(AIPlayer.ID);
		addPlayer(AIPlayer.ID, deck);
	}

	private void addRealPlayer(ServerPlayer player, ItemStack stack) {
		var id = player.getUUID();
		DeckData.get(stack).ifPresent(data -> {
			List<Card> deck = new ArrayList<>();

			for (int i = 0; i < data.getSlots(); i++) {
				var item = data.getStackInSlot(i);
				if (item.isEmpty()) {
					player.sendMessage(new TranslatableComponent(Helper.chat("not_enough_cards")), id);
					break;
				}
				CardData.getType(item).ifPresent(type -> deck.add(Cards.getInstance(false).get(type).create()));
			}

			if (deck.size() != DeckData.CAPACITY) {
				return;
			}

			addPlayer(id, deck);
		});
	}

	private OpenGameMessage createOpenGameMessage(UUID id) {
		var state1 = state.getPlayerStates().get(0);
		var state2 = state.getPlayerStates().get(1);

		return new OpenGameMessage(
				List.of(state1.toMessage(!state1.getId().equals(id)), state2.toMessage(!state2.getId().equals(id))),
				getBlockPos());
	}
}