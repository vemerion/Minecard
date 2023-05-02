package mod.vemerion.minecard.network;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardProperty;
import mod.vemerion.minecard.game.MessagePlayerState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface GameClient {

	public void animation(int originId, List<Integer> targets, ResourceLocation rl);

	public void updateDecks(Map<UUID, Integer> sizes);

	public void combat(UUID attackerId, int attackerCardId, UUID targetId, int targetCardId);

	public void setProperties(UUID id, int cardId, Map<CardProperty, Integer> properties);

	public void gameOver();

	public void drawCards(UUID id, List<Card> cards, boolean shrinkDeck);

	public void updateCard(Card received);

	public void setReady(UUID id, List<Integer> cards);

	public void placeCard(UUID id, Card card, int leftId);

	public void setResources(UUID id, int resources, int maxResources);

	public void setCurrent(UUID current);

	public void openGame(List<MessagePlayerState> state, BlockPos pos);
}