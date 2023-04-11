package mod.vemerion.minecard.game;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.SerializableUUID;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

public class AdditionalCardData {

	public static final NoData EMPTY = new NoData();

	public static final Codec<AdditionalCardData> CODEC = chain(List.of(new CodecEntry<>(IdData.CODEC, IdData.class),
			new CodecEntry<>(ItemData.CODEC, ItemData.class), new CodecEntry<>(NoData.CODEC, NoData.class)));

	public static class NoData extends AdditionalCardData {
		public static final Codec<NoData> CODEC = Codec.unit(EMPTY);

		private NoData() {

		}
	}

	public static class IdData extends AdditionalCardData {

		public static final Codec<IdData> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(SerializableUUID.CODEC.fieldOf("id").forGetter(IdData::getId)).apply(instance, IdData::new));

		private UUID id;

		public IdData(UUID id) {
			this.id = id;
		}

		public UUID getId() {
			return id;
		}
	}

	public static class ItemData extends AdditionalCardData {

		public static final Codec<ItemData> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(ItemData::getItem))
				.apply(instance, ItemData::new));

		private Item item;

		public ItemData(Item item) {
			this.item = item;
		}

		public Item getItem() {
			return item;
		}
	}

	public static class TestData extends AdditionalCardData {

		public static final Codec<TestData> CODEC = RecordCodecBuilder
				.create(instance -> instance
						.group(Codec.INT.fieldOf("field1").forGetter(TestData::getField1),
								Codec.STRING.fieldOf("field2").forGetter(TestData::getField2))
						.apply(instance, TestData::new));

		private int field1;
		private String field2;

		public TestData(int field1, String field2) {
			this.field1 = field1;
			this.field2 = field2;
		}

		public int getField1() {
			return field1;
		}

		public String getField2() {
			return field2;
		}
	}

	private static record CodecEntry<T extends AdditionalCardData> (Codec<T> codec, Class<T> type) {

	}

	private static Codec<AdditionalCardData> chain(List<CodecEntry<? extends AdditionalCardData>> codecs) {
		if (codecs.size() < 2)
			throw new IllegalArgumentException("Invalid list");

		Codec<AdditionalCardData> codec = start(codecs.get(codecs.size() - 1));

		for (int i = codecs.size() - 2; i >= 0; i--) {
			codec = combine(codecs.get(i), codec);
		}

		return codec;
	}

	private static <T extends AdditionalCardData> Codec<AdditionalCardData> start(CodecEntry<T> entry) {
		return entry.codec.flatComapMap(Function.identity(),
				data -> entry.type.isInstance(data) ? DataResult.success(entry.type.cast(data))
						: DataResult.error("Invalid additional card data type"));
	}

	private static <T extends AdditionalCardData> Codec<AdditionalCardData> combine(CodecEntry<T> entry,
			Codec<AdditionalCardData> codec) {
		return Codec.either(entry.codec, codec).xmap(either -> either.map(Function.identity(), Function.identity()),
				data -> entry.type.isInstance(data) ? Either.left(entry.type.cast(data)) : Either.right(data));
	}
}
