package mod.vemerion.minecard.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.capability.DeckData;
import mod.vemerion.minecard.game.AdditionalCardData;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.GameState;
import mod.vemerion.minecard.game.PlayerState;
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

public class GameBlockEntity extends BlockEntity {

	private static final int START_HAND_SIZE = 5;

	GameState state;

	private Set<UUID> receivers;

	public GameBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(ModBlockEntities.GAME.get(), pWorldPosition, pBlockState);
		receivers = new HashSet<>();
		state = new GameState();
	}

	public boolean canReceiveMessage(ServerPlayer player) {
		return state.getPlayerStates().size() > 1;
	}

	public boolean isPlayerTurn(ServerPlayer player) {
		return state.getCurrentPlayer().equals(player.getUUID());
	}

	public void endTurn(ServerPlayer player) {
		state.endTurn(getReceivers());
		var current = state.getCurrentPlayerState();
		for (var receiver : getReceivers()) {
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver),
					new NewTurnMessage(state.getCurrentPlayer()));
			Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver), new SetResourcesMessage(
					state.getCurrentPlayer(), current.getResources(), current.getMaxResources()));
			if (!current.getBoard().isEmpty()) {
				Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) receiver),
						new SetReadyMessage(current.getId(), current.getReady()));
			}
		}

	}

	public void playCard(ServerPlayer player, int card, int position) {
		state.getCurrentPlayerState().playCard(getReceivers(), card, position);
	}

	public void attack(ServerPlayer player, int attacker, int target) {
		state.attack(getReceivers(), attacker, target);

		if (state.isGameOver()) {
			for (var receiver : getReceivers()) {
				Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> receiver), new GameOverMessage());
			}

			state = new GameState();
		}
	}

	public void closeGame(ServerPlayer sender) {
		receivers.remove(sender.getUUID());
	}

	private List<ServerPlayer> getReceivers() {
		var list = new ArrayList<ServerPlayer>();
		for (var id : receivers) {
			var player = level.getPlayerByUUID(id);
			if (player != null)
				list.add((ServerPlayer) player);
		}
		return list;
	}

	public void open(ServerPlayer player, ItemStack stack) {
		var id = player.getUUID();
		if (state.getPlayerStates().stream().anyMatch(s -> s.getId() == id)) {
			if (state.getPlayerStates().size() == 1) {
				player.sendMessage(new TranslatableComponent(Helper.chat("not_enough_players")), id);
			} else {
				receivers.add(player.getUUID());
				Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), createOpenGameMessage(id));
				Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
						new NewTurnMessage(state.getCurrentPlayer()));
			}
		} else if (state.getPlayerStates().size() > 1) {
			player.sendMessage(new TranslatableComponent(Helper.chat("game_ongoing")), id);
		} else if (stack.is(ModItems.DECK.get())) {
			addPlayer(player, stack);
		} else {
			player.sendMessage(new TranslatableComponent(Helper.chat("need_deck")), id);
		}
	}

	private void addPlayer(ServerPlayer player, ItemStack stack) {
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

			Collections.shuffle(deck);
			List<Card> hand = new ArrayList<>();
			for (int i = 0; i < START_HAND_SIZE; i++) {
				hand.add(deck.remove(deck.size() - 1));
			}

			List<Card> board = new ArrayList<>();
			var playerCard = Cards.getInstance(false).get(EntityType.PLAYER).create();
			playerCard.setAdditionalData(new AdditionalCardData.IdData(player.getUUID()));
			board.add(playerCard);

			state.getPlayerStates().add(new PlayerState(id, deck, hand, board, 1, 1));
		});
	}

	private OpenGameMessage createOpenGameMessage(UUID id) {
		var yourState = state.getPlayerStates().stream().filter(s -> s.getId() == id).findAny().get();
		var enemyState = state.getPlayerStates().stream().filter(s -> s.getId() != id).findAny().get();

		return new OpenGameMessage(List.of(yourState.toMessage(false), enemyState.toMessage(true)), getBlockPos());
	}
}