package mod.vemerion.minecard.capability;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;

import mod.vemerion.minecard.game.GameUtil;
import mod.vemerion.minecard.network.Network;
import mod.vemerion.minecard.network.StatMessage;
import net.minecraft.core.Direction;
import net.minecraft.core.SerializableUUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.PacketDistributor;

// Player might not be online while stats need to be updated, so need to put capability on level instead of player
public class StatsData implements INBTSerializable<CompoundTag> {

	public static final Capability<StatsData> CAPABILITY = CapabilityManager.get(new CapabilityToken<StatsData>() {
	});

	public static final Codec<Map<UUID, PlayerStats>> MAP_CODEC = GameUtil.mapCodec(SerializableUUID.CODEC,
			PlayerStats.CODEC);

	private Map<UUID, PlayerStats> stats;

	public StatsData() {
		stats = new HashMap<>();
	}

	private PlayerStats get(UUID player) {
		return stats.computeIfAbsent(player, uuid -> new PlayerStats());
	}

	public static LazyOptional<PlayerStats> get(Level level, UUID player) {
		return level.getServer().getLevel(Level.OVERWORLD).getCapability(CAPABILITY).lazyMap(c -> c.get(player));
	}

	public static void inc(Level level, UUID player, ResourceLocation id, Optional<UUID> enemy) {
		level.getServer().getLevel(Level.OVERWORLD).getCapability(CAPABILITY).ifPresent(c -> {
			var key = new PlayerStats.Key(id, enemy);
			var value = c.get(player).inc(key);

			if (level.getPlayerByUUID(player) instanceof ServerPlayer serverPlayer) {
				Network.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new StatMessage(key, value));
			}
		});
	}

	@Override
	public CompoundTag serializeNBT() {
		var tag = new CompoundTag();
		tag.put("stats", MAP_CODEC.encodeStart(NbtOps.INSTANCE, stats).getOrThrow(false, s -> {
		}));
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		stats = MAP_CODEC.parse(NbtOps.INSTANCE, nbt.get("stats")).getOrThrow(false, s -> {
		});
	}

	public static class Provider implements ICapabilitySerializable<CompoundTag> {

		public Provider() {
		}

		private LazyOptional<StatsData> instance = LazyOptional.of(StatsData::new);

		@Override
		public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
			return CAPABILITY.orEmpty(cap, instance);
		}

		@Override
		public CompoundTag serializeNBT() {
			return instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!"))
					.serializeNBT();
		}

		@Override
		public void deserializeNBT(CompoundTag nbt) {
			instance.orElseThrow(() -> new IllegalArgumentException("LazyOptional cannot be empty!"))
					.deserializeNBT(nbt);
		}
	}
}
