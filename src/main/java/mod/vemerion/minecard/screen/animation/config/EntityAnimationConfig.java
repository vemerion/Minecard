package mod.vemerion.minecard.screen.animation.config;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.EntityAnimation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityAnimationConfig extends AnimationConfig {

	public static final Codec<EntityAnimationConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ForgeRegistries.ENTITY_TYPES.getCodec().fieldOf("entity")
					.forGetter(EntityAnimationConfig::getEntity),
					Codec.BOOL.fieldOf("moving").forGetter(EntityAnimationConfig::isMoving),
					Codec.INT.fieldOf("duration").forGetter(EntityAnimationConfig::getDuration),
					Codec.INT.fieldOf("size").forGetter(EntityAnimationConfig::getSize),
					Codec.INT.optionalFieldOf("sound_delay", 1).forGetter(EntityAnimationConfig::getSoundDelay),
					ForgeRegistries.SOUND_EVENTS.getCodec().optionalFieldOf("start_sound")
							.forGetter(EntityAnimationConfig::getStartSound),
					ForgeRegistries.SOUND_EVENTS.getCodec().optionalFieldOf("duration_sound")
							.forGetter(EntityAnimationConfig::getDurationSound),
					ForgeRegistries.SOUND_EVENTS.getCodec().optionalFieldOf("impact_sound")
							.forGetter(EntityAnimationConfig::getImpactSound),
					EntityAnimation.SpecialAnimation.CODEC
							.optionalFieldOf("special_animation", EntityAnimation.SpecialAnimation.NONE)
							.forGetter(EntityAnimationConfig::getSpecialAnimation))
			.apply(instance, EntityAnimationConfig::new));

	private final EntityType<?> entity;
	private final boolean moving;
	private final int duration;
	private final int size;
	private final int soundDelay;
	private final Optional<SoundEvent> startSound;
	private final Optional<SoundEvent> durationSound;
	private final Optional<SoundEvent> impactSound;
	private final EntityAnimation.SpecialAnimation specialAnimation;

	public EntityAnimationConfig(EntityType<?> entity, boolean moving, int duration, int size, int soundDelay,
			Optional<SoundEvent> startSound, Optional<SoundEvent> durationSound, Optional<SoundEvent> impactSound,
			EntityAnimation.SpecialAnimation specialAnimation) {
		this.moving = moving;
		this.entity = entity;
		this.duration = duration;
		this.size = size;
		this.soundDelay = soundDelay;
		this.startSound = startSound;
		this.durationSound = durationSound;
		this.impactSound = impactSound;
		this.specialAnimation = specialAnimation;
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.CHARGE.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		for (var target : targets)
			game.addAnimation(
					new EntityAnimation(
							game.getMinecraft(), moving
									? (origin != null
											? origin.getDestination().add(
													new Vec2(ClientCard.CARD_WIDTH / 2, ClientCard.CARD_HEIGHT / 2))
											: new Vec2(game.width / 2, game.height / 2))
									: null,
							() -> new Vec2(target.getPosition().x + ClientCard.CARD_WIDTH / 2,
									target.getPosition().y + ClientCard.CARD_HEIGHT / 2),
							entity, duration, size, soundDelay, startSound, durationSound, impactSound,
							specialAnimation, () -> {
							}));
	}

	public EntityType<?> getEntity() {
		return entity;
	}

	public boolean isMoving() {
		return moving;
	}

	public int getDuration() {
		return duration;
	}

	public int getSize() {
		return size;
	}

	public int getSoundDelay() {
		return soundDelay;
	}

	public Optional<SoundEvent> getStartSound() {
		return startSound;
	}

	public Optional<SoundEvent> getDurationSound() {
		return durationSound;
	}

	public Optional<SoundEvent> getImpactSound() {
		return impactSound;
	}

	public EntityAnimation.SpecialAnimation getSpecialAnimation() {
		return specialAnimation;
	}
}
