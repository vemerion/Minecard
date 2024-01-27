package mod.vemerion.minecard.game;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.network.PlayerChoiceMessage;

public class PlayerChoice {
	private boolean active;
	private Semaphore mainSignal = new Semaphore(0); // Signal that main thread is waiting on
	private Semaphore subSignal = new Semaphore(0); // Signal that sub thread is waiting on
	private Response response;
	private Request request;

	public void setActive(boolean b) {
		active = b;
	}

	public void respond(List<Receiver> receivers, int selected) {
		if (response != null)
			return;
		response = new Response(receivers, selected);
		subSignal.release();
		try {
			mainSignal.acquire();
		} catch (InterruptedException e) {
			Main.LOGGER.debug("Unexpected interrupt of main thread");
		}
	}

	public void mainWaiting() {
		try {
			mainSignal.acquire();
		} catch (InterruptedException e) {
			Main.LOGGER.debug("Unexpected interrupt of main thread");
		}
	}

	public void wakeMain() {
		mainSignal.release();
	}

	public void resend(Receiver receiver) {
		if (request != null) {
			receiver.receiver(new PlayerChoiceMessage(request.textKey, request.cards, request.targeting));
		}
	}

	public Optional<Card> make(List<Receiver> receivers, String textKey, List<Card> cards, boolean targeting,
			Random rand, UUID player) {
		if (cards.isEmpty())
			return Optional.empty();

		if (!active)
			return Optional.of(cards.get(rand.nextInt(cards.size())));

		request = new Request(textKey, cards, targeting);
		for (var receiver : receivers) {
			if (receiver.getId().equals(player)) {
				receiver.receiver(new PlayerChoiceMessage(textKey, cards, targeting));
			}
		}
		mainSignal.release();
		try {
			subSignal.acquire();
		} catch (InterruptedException e) {
			Main.LOGGER.debug("Unexpected interrupt of sub thread");
			request = null;
			return Optional.empty();
		}
		request = null;
		receivers.clear();
		receivers.addAll(response.receivers);
		var result = cards.stream().filter(c -> c.getId() == response.selected).findFirst()
				.or(() -> Optional.of(cards.get(rand.nextInt(cards.size()))));
		response = null;
		return result;
	}

	public static record Response(List<Receiver> receivers, int selected) {

	}

	public static record Request(String textKey, List<Card> cards, boolean targeting) {

	}
}
