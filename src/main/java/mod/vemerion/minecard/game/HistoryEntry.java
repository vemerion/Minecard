package mod.vemerion.minecard.game;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.SerializableUUID;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

public class HistoryEntry {
	public static final Codec<HistoryEntry> CODEC = ExtraCodecs
			.lazyInitializedCodec(
					() -> RecordCodecBuilder
							.create(instance -> instance
									.group(ItemStack.CODEC.fieldOf("icon").forGetter(HistoryEntry::getIcon),
											SerializableUUID.CODEC.fieldOf("playerId")
													.forGetter(HistoryEntry::getPlayerId),
											Card.CODEC.fieldOf("card").forGetter(HistoryEntry::getCard),
											Codec.list(Card.CODEC).fieldOf("targets")
													.forGetter(HistoryEntry::getTargets))
									.apply(instance, HistoryEntry::new)));

	private ItemStack icon;
	private UUID playerId;
	private Card card;
	private List<Card> targets;

	public HistoryEntry(ItemStack icon, UUID playerId, Card card, List<Card> targets) {
		this.icon = icon;
		this.playerId = playerId;
		this.card = new Card(card);
		this.targets = targets.stream().map(c -> new Card(c)).toList();
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

	public List<Card> getTargets() {
		return targets;
	}
}
