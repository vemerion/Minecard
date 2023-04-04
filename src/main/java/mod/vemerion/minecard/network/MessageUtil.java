package mod.vemerion.minecard.network;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.game.Card;
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
		buffer.writeInt(card.getId());
	}

	public static Card decodeCard(FriendlyByteBuf buffer) {
		var nbt = buffer.readNbt();
		Card card;
		if (nbt.contains("value"))
			card = Card.CODEC.parse(NbtOps.INSTANCE, nbt.get("value")).getOrThrow(false, MessageUtil::onError);
		else {
			card = Cards.EMPTY_CARD_TYPE.create();
		}
		card.setId(buffer.readInt());
		return card;
	}

	public static <T> void encode(FriendlyByteBuf buffer, T value, Codec<T> codec) {
		var tag = new CompoundTag();
		tag.put("value", codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, MessageUtil::onError));
		buffer.writeNbt(tag);
	}

	public static <T> T decode(FriendlyByteBuf buffer, Codec<T> codec) {
		var nbt = buffer.readNbt();
		return codec.parse(NbtOps.INSTANCE, nbt.get("value")).getOrThrow(false, MessageUtil::onError);
	}

	private static void onError(String msg) {
	}
}
