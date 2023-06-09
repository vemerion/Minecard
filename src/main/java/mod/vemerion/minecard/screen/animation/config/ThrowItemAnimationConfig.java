package mod.vemerion.minecard.screen.animation.config;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.ThrowItemAnimation;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.ForgeRegistries;

public class ThrowItemAnimationConfig extends AnimationConfig {

	public static final Codec<ThrowItemAnimationConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ItemStack.CODEC.fieldOf("item").forGetter(ThrowItemAnimationConfig::getStack),
					ForgeRegistries.SOUND_EVENTS.getCodec().optionalFieldOf("start_sound")
							.forGetter(ThrowItemAnimationConfig::getStartSound),
					ForgeRegistries.SOUND_EVENTS.getCodec().optionalFieldOf("impact_sound")
							.forGetter(ThrowItemAnimationConfig::getImpactSound),
					ResourceLocation.CODEC.optionalFieldOf("end_animation")
							.forGetter(ThrowItemAnimationConfig::getEndAnimation))
			.apply(instance, ThrowItemAnimationConfig::new));

	private final ItemStack stack;
	private final Optional<SoundEvent> startSound;
	private final Optional<SoundEvent> impactSound;
	private final Optional<ResourceLocation> endAnimation;

	public ThrowItemAnimationConfig(ItemStack stack, Optional<SoundEvent> startSound, Optional<SoundEvent> impactSound,
			Optional<ResourceLocation> endAnimation) {
		this.stack = stack;
		this.startSound = startSound;
		this.impactSound = impactSound;
		this.endAnimation = endAnimation;
	}

	public ThrowItemAnimationConfig(Item item, Optional<SoundEvent> startSound, Optional<SoundEvent> impactSound,
			Optional<ResourceLocation> endAnimation) {
		this(item.getDefaultInstance(), startSound, impactSound, endAnimation);
	}

	public ThrowItemAnimationConfig(Item item) {
		this(item.getDefaultInstance(), Optional.empty(), Optional.empty(), Optional.empty());
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.THROW_ITEM.get();
	}

	public ItemStack getStack() {
		return stack;
	}

	public Optional<SoundEvent> getStartSound() {
		return startSound;
	}

	public Optional<SoundEvent> getImpactSound() {
		return impactSound;
	}

	public Optional<ResourceLocation> getEndAnimation() {
		return endAnimation;
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		var pos = origin != null
				? origin.getPosition().add(new Vec2(ClientCard.CARD_WIDTH / 2, ClientCard.CARD_HEIGHT / 2))
				: new Vec2(game.width / 2, game.height / 2);

		startSound.ifPresent(s -> {
			game.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(s, 1, 1));
		});
		for (var target : targets)
			game.addAnimation(new ThrowItemAnimation(game.getMinecraft(), stack, pos, target, () -> {
				endAnimation.ifPresent(anim -> {
					game.animation(origin == null ? -1 : origin.getId(), List.of(target.getId()), anim);
				});
				impactSound.ifPresent(s -> {
					game.getMinecraft().getSoundManager().play(SimpleSoundInstance.forUI(s, 1, 1));
				});
			}));
	}
}
