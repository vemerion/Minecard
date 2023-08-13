package mod.vemerion.minecard.capability;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.game.GameUtil;
import net.minecraft.core.SerializableUUID;
import net.minecraft.resources.ResourceLocation;

public class PlayerStats {

	public static final Codec<PlayerStats> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(GameUtil.mapCodec(Key.CODEC, Codec.INT).fieldOf("stats").forGetter(PlayerStats::getStats))
			.apply(instance, PlayerStats::new));

	private Map<Key, Integer> stats;

	public PlayerStats(Map<Key, Integer> stats) {
		this.stats = stats;
	}

	public PlayerStats() {
		this(new HashMap<>());
	}

	public Map<Key, Integer> getStats() {
		return stats;
	}

	public int inc(Key key) {
		return stats.merge(key, 1, (a, b) -> a + 1);
	}

	public void put(Key key, int value) {
		stats.put(key, value);
	}

	public int get(ResourceLocation id, Optional<UUID> enemy) {
		return stats.getOrDefault(new Key(id, enemy), 0);
	}

	public static class Key {

		public static final ResourceLocation WINS = new ResourceLocation(Main.MODID, "wins");
		public static final ResourceLocation LOSSES = new ResourceLocation(Main.MODID, "losses");

		public static final Codec<Key> CODEC = RecordCodecBuilder.create(instance -> instance
				.group(ResourceLocation.CODEC.fieldOf("id").forGetter(Key::getId),
						SerializableUUID.CODEC.optionalFieldOf("enemy").forGetter(Key::getEnemy))
				.apply(instance, Key::new));

		private ResourceLocation id;
		private Optional<UUID> enemy;

		public Key(ResourceLocation id, Optional<UUID> enemy) {
			this.id = id;
			this.enemy = enemy;
		}

		public ResourceLocation getId() {
			return id;
		}

		public Optional<UUID> getEnemy() {
			return enemy;
		}

		@Override
		public int hashCode() {
			return Objects.hash(id, enemy);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof Key other) {
				return Objects.equals(id, other.id) && Objects.equals(enemy, other.enemy);
			}
			return false;
		}
	}
}
