package mod.vemerion.minecard.network;

import mod.vemerion.minecard.game.Card;
import mod.vemerion.minecard.game.CardType;
import mod.vemerion.minecard.game.Cards;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;

public class MessageUtil {

	public static void encodeCard(FriendlyByteBuf buffer, Card card) {
		var tag = new CompoundTag();
		if (card.getType() != null)
			tag.put("value", Card.CODEC.encodeStart(NbtOps.INSTANCE, card).getOrThrow(false, MessageUtil::onError));
		buffer.writeNbt(tag);
	}

	public static Card decodeCard(FriendlyByteBuf buffer) {
		var nbt = buffer.readNbt();
		if (nbt.contains("value"))
			return Card.CODEC.parse(NbtOps.INSTANCE, nbt.get("value")).getOrThrow(false, MessageUtil::onError);
		else
			return Cards.EMPTY_CARD;
	}

	public static void encodeCardType(FriendlyByteBuf buffer, CardType card) {
		var tag = new CompoundTag();
		tag.put("value", CardType.CODEC.encodeStart(NbtOps.INSTANCE, card).getOrThrow(false, MessageUtil::onError));
		buffer.writeNbt(tag);
	}

	public static CardType decodeCardType(FriendlyByteBuf buffer) {
		var nbt = buffer.readNbt();
		return CardType.CODEC.parse(NbtOps.INSTANCE, nbt.get("value")).getOrThrow(false, MessageUtil::onError);
	}

	private static void onError(String msg) {
	}
}
