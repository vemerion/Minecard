package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import mod.vemerion.minecard.game.ability.CardAbility;
import mod.vemerion.minecard.network.PlayerChoiceMessage;

public class PlayerChoices {

	private UUID player;
	private Card card;
	private int leftId;
	private List<Choice> choices = new ArrayList<>();

	public PlayerChoices(UUID player, Card card, int leftId) {
		this.player = player;
		this.card = card;
		this.leftId = leftId;
	}

	public void addChoice(List<Receiver> receivers, CardAbility ability, List<Card> cards, boolean targeting) {
		if (choices.stream().anyMatch(c -> c.ability == ability))
			return;

		choices.add(new Choice(ability, cards));
		for (var receiver : receivers) {
			if (receiver.getId().equals(player)) {
				receiver.receiver(new PlayerChoiceMessage(choices.size() - 1, ability, cards, targeting));
			}
		}
	}

	public long getPendingCount() {
		return choices.stream().filter(c -> c.selected == -1).count();
	}

	public Optional<Card> getSelected(CardAbility ability) {
		for (var choice : choices)
			if (choice.ability == ability)
				return choice.getSelected();
		return Optional.empty();
	}

	public void makeChoice(int id, int selected) {
		if (id < 0 || id >= choices.size() || choices.get(id).cards.stream().noneMatch(c -> c.getId() == selected))
			return;
		choices.get(id).selected = selected;
	}

	public UUID getPlayer() {
		return player;
	}

	public Card getCard() {
		return card;
	}

	public int getLeftId() {
		return leftId;
	}

	private static class Choice {

		private CardAbility ability;
		private List<Card> cards;
		private int selected = -1;

		public Choice(CardAbility ability, List<Card> cards) {
			this.ability = ability;
			this.cards = cards;
		}

		private Optional<Card> getSelected() {
			for (var card : cards)
				if (card.getId() == selected)
					return Optional.of(card);
			return Optional.empty();
		}
	}
}
