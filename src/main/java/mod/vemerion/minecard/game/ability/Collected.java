package mod.vemerion.minecard.game.ability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mod.vemerion.minecard.game.Card;

public class Collected {
	private Map<Integer, List<Card>> collected;

	public Collected() {
		collected = new HashMap<>();
	}

	public List<Card> get(int i) {
		collected.putIfAbsent(i, new ArrayList<>());
		return collected.get(i);
	}

	public void clear(int i) {
		collected.put(i, new ArrayList<>());
	}
}
