package mod.vemerion.minecard.blockentity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import mod.vemerion.minecard.capability.CardData;
import mod.vemerion.minecard.capability.DeckData;
import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.Cards;
import mod.vemerion.minecard.game.ClientState;
import mod.vemerion.minecard.game.GameState;
import mod.vemerion.minecard.game.PlayerState;
import mod.vemerion.minecard.helper.Helper;
import mod.vemerion.minecard.init.ModBlockEntities;
import mod.vemerion.minecard.init.ModItems;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.NewTurnMessage;
import mod.vemerion.minecard.network.OpenGameMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

public class GameBlockEntity extends BlockEntity {

	private static final int START_HAND_SIZE = 5;

	GameState state;

	public GameBlockEntity(BlockPos pWorldPosition, BlockState pBlockState) {
		super(ModBlockEntities.GAME.get(), pWorldPosition, pBlockState);
		state = new GameState();
	}

	public void open(ServerPlayer player, ItemStack stack) {
		var id = player.getUUID();
		if (state.getPlayerStates().stream().anyMatch(s -> s.getId() == id)) {
			if (state.getPlayerStates().size() == 1) {
				player.sendMessage(new TranslatableComponent(Helper.chat("not_enough_players")), id);
			} else {
				Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), createOpenGateMessage(id));
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
				CardData.getType(item).ifPresent(type -> deck.add(Cards.getInstance().get(type).copy()));
			}

			if (deck.size() != DeckData.CAPACITY) {
				return;
			}

			Collections.shuffle(deck);
			List<Card> hand = new ArrayList<>();
			for (int i = 0; i < START_HAND_SIZE; i++) {
				hand.add(deck.remove(deck.size() - 1));
			}

			state.getPlayerStates().add(new PlayerState(id, deck, hand, new ArrayList<>()));
		});
	}

	private OpenGameMessage createOpenGateMessage(UUID id) {
		var yourState = state.getPlayerStates().stream().filter(s -> s.getId() == id).findAny().get();
		var enemyState = state.getPlayerStates().stream().filter(s -> s.getId() != id).findAny().get();

		return new OpenGameMessage(new ClientState(enemyState.getDeck().size(), yourState.getDeck().size(),
				enemyState.getHand().size(), yourState.getHand(), enemyState.getBoard(), yourState.getBoard()));
	}

}