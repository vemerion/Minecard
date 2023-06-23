package mod.vemerion.minecard.network;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.HistoryEntry;
import mod.vemerion.minecard.game.MessagePlayerState;
import mod.vemerion.minecard.game.ability.CardAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public interface GameClient {

	public static record Choice(int id, CardAbility ability, List<Card> cards, boolean targeting) {
	}

	public void animation(int originId, List<Integer> targets, ResourceLocation rl);

	public void updateDecks(Map<UUID, Integer> sizes);

	public void combat(UUID attackerId, int attackerCardId, UUID targetId, int targetCardId);

	public void setProperties(UUID id, int cardId, Map<ResourceLocation, Integer> properties);

	public void gameOver();

	public void drawCards(UUID id, List<Card> cards, boolean shrinkDeck);

	public void updateCard(Card received);

	public void placeCard(UUID id, Card card, int leftId);

	public void setResources(UUID id, int resources, int maxResources);

	public void setCurrent(UUID current);

	public void openGame(List<MessagePlayerState> state, BlockPos pos);

	public void playerChoice(Choice choice);

	public void history(HistoryEntry entry);

	public void mulliganDone(UUID id);
}
