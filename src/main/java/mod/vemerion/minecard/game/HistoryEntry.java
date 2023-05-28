package mod.vemerion.minecard.game;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.SerializableUUID;
import net.minecraft.util.ExtraCodecs;

public class HistoryEntry {
	public static enum Type {
		PLAY_CARD("play_card"), ATTACK("attack"), ABILITY("ability");

		public static final Codec<Type> CODEC = GameUtil.enumCodec(Type.class, Type::getName);

		private String name;

		private Type(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static final Codec<HistoryEntry> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder
							.create(instance -> instance
									.group(Type.CODEC.fieldOf("type").forGetter(HistoryEntry::getType),
											SerializableUUID.CODEC.fieldOf("playerId")
													.forGetter(HistoryEntry::getPlayerId),
											Card.CODEC.fieldOf("card").forGetter(HistoryEntry::getCard),
											Codec.list(Card.CODEC).fieldOf("targets")
													.forGetter(HistoryEntry::getTargets))
									.apply(instance, HistoryEntry::new)));

	private Type type;
	private UUID playerId;
	private Card card;
	private List<Card> targets;

	public HistoryEntry(Type type, UUID playerId, Card card, List<Card> targets) {
		this.type = type;
		this.playerId = playerId;
		this.card = new Card(card);
		this.targets = targets.stream().map(c -> new Card(c)).toList();
	}

	public Type getType() {
		return type;
	}

	public UUID getPlayerId() {
		return playerId;
	}

	public Card getCard() {
		return card;
	}

	public List<Card> getTargets() {
		return targets;
	}
}
