package mod.vemerion.minecard.game;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class HistoryEntry {

	public static enum Visibility {
		NONE("none"), YOU("you"), ENEMY("enemy"), ALL("all");

		public static Codec<Visibility> CODEC = GameUtil.enumCodec(Visibility.class, Visibility::getName);

		private String name;

		private Visibility(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	};

	public static record Target(Card card, Visibility visiblility) {
		public static final Codec<Target> CODEC = ExtraCodecs
				.lazyInitializedCodec(() -> RecordCodecBuilder.create(instance -> instance
						.group(Card.CODEC.fieldOf("card").forGetter(Target::card),
								Visibility.CODEC.fieldOf("visiblility").forGetter(Target::visiblility))
						.apply(instance, Target::new)));
	}

	public static final Codec<HistoryEntry> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder
							.create(instance -> instance
									.group(ItemStack.CODEC.fieldOf("icon").forGetter(HistoryEntry::getIcon),
											UUIDUtil.CODEC.fieldOf("playerId").forGetter(HistoryEntry::getPlayerId),
											Card.CODEC.fieldOf("card").forGetter(HistoryEntry::getCard),
											Codec.list(Target.CODEC).fieldOf("targets")
													.forGetter(HistoryEntry::getTargets))
									.apply(instance, HistoryEntry::new)));

	private ItemStack icon;
	private UUID playerId;
	private Card card;
	private List<Target> targets;

	public HistoryEntry(ItemStack icon, UUID playerId, Card card, List<Target> targets) {
		this.icon = icon;
		this.playerId = playerId;
		this.card = new Card(card);
		this.targets = targets.stream().map(t -> new Target(new Card(t.card), t.visiblility)).toList();
	}

	public ItemStack getIcon() {
		return icon;
	}

	public UUID getPlayerId() {
		return playerId;
	}

	public Card getCard() {
		return card;
	}

	public List<Target> getTargets() {
		return targets;
	}

	public HistoryEntry censor(UUID receiver, boolean spectator) {
		return new HistoryEntry(icon, playerId, card, targets.stream().map(t -> new Target(
				shouldCensor(t, receiver, spectator) ? Cards.EMPTY_CARD_TYPE.create().setId(t.card.getId()) : t.card,
				t.visiblility)).toList());
	}

	private boolean shouldCensor(Target target, UUID receiver, boolean spectator) {
		switch (target.visiblility) {
		case ALL:
			return false;
		case ENEMY:
			return spectator || playerId.equals(receiver);
		case NONE:
			return true;
		case YOU:
			return !playerId.equals(receiver);
		}
		return true;
	}
}
