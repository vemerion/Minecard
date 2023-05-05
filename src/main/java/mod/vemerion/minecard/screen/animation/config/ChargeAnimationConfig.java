package mod.vemerion.minecard.screen.animation.config;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.ChargeAnimation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.ForgeRegistries;

public class ChargeAnimationConfig extends AnimationConfig {

	public static final Codec<ChargeAnimationConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ForgeRegistries.ENTITIES.getCodec().fieldOf("entity").forGetter(ChargeAnimationConfig::getEntity),
					Codec.INT.fieldOf("duration").forGetter(ChargeAnimationConfig::getDuration),
					Codec.INT.fieldOf("size").forGetter(ChargeAnimationConfig::getSize),
					ForgeRegistries.SOUND_EVENTS.getCodec().optionalFieldOf("sound")
							.forGetter(ChargeAnimationConfig::getSound),
					ForgeRegistries.SOUND_EVENTS.getCodec().optionalFieldOf("impact_sound")
							.forGetter(ChargeAnimationConfig::getImpactSound))
			.apply(instance, ChargeAnimationConfig::new));

	private final EntityType<?> entity;
	private final int duration;
	private final int size;
	private final Optional<SoundEvent> sound;
	private final Optional<SoundEvent> impactSound;

	public ChargeAnimationConfig(EntityType<?> entity, int duration, int size, Optional<SoundEvent> sound,
			Optional<SoundEvent> impactSound) {
		this.entity = entity;
		this.duration = duration;
		this.size = size;
		this.sound = sound;
		this.impactSound = impactSound;
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.CHARGE.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		for (var target : targets)
			game.addAnimation(new ChargeAnimation(game.getMinecraft(),
					origin.getPosition().add(new Vec2(ClientCard.CARD_WIDTH / 2, ClientCard.CARD_HEIGHT / 2)), target,
					entity, duration, size, sound, impactSound, () -> {
					}));
	}

	public EntityType<?> getEntity() {
		return entity;
	}

	public int getDuration() {
		return duration;
	}

	public int getSize() {
		return size;
	}

	public Optional<SoundEvent> getSound() {
		return sound;
	}

	public Optional<SoundEvent> getImpactSound() {
		return impactSound;
	}
}
