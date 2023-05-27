package mod.vemerion.minecard.game;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

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
											Card.CODEC.fieldOf("card").forGetter(HistoryEntry::getCard),
											Codec.list(Card.CODEC).fieldOf("targets")
													.forGetter(HistoryEntry::getTargets))
									.apply(instance, HistoryEntry::new)));

	private Type type;
	private Card card;
	private List<Card> targets;

	public HistoryEntry(Type type, Card card, List<Card> targets) {
		this.type = type;
		this.card = card;
		this.targets = targets;
	}

	public Type getType() {
		return type;
	}

	public Card getCard() {
		return card;
	}

	public List<Card> getTargets() {
		return targets;
	}
}
