package mod.vemerion.minecard.screen.animation.config;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.ThrowItemAnimation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec2;

public class ThrowItemAnimationConfig extends AnimationConfig {

	public static final Codec<ThrowItemAnimationConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ItemStack.CODEC.fieldOf("item").forGetter(ThrowItemAnimationConfig::getStack),
					ResourceLocation.CODEC.optionalFieldOf("end_animation")
							.forGetter(ThrowItemAnimationConfig::getEndAnimation))
			.apply(instance, ThrowItemAnimationConfig::new));

	private final ItemStack stack;
	private final Optional<ResourceLocation> endAnimation;

	public ThrowItemAnimationConfig(ItemStack stack, Optional<ResourceLocation> endAnimation) {
		this.stack = stack;
		this.endAnimation = endAnimation;
	}

	public ThrowItemAnimationConfig(Item item, Optional<ResourceLocation> endAnimation) {
		this(item.getDefaultInstance(), endAnimation);
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.THROW_ITEM.get();
	}

	public ItemStack getStack() {
		return stack;
	}

	public Optional<ResourceLocation> getEndAnimation() {
		return endAnimation;
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		var pos = origin != null
				? origin.getPosition().add(new Vec2(ClientCard.CARD_WIDTH / 2, ClientCard.CARD_HEIGHT / 2))
				: new Vec2(game.width / 2, game.height / 2);

		for (var target : targets)
			game.addAnimation(
					new ThrowItemAnimation(game.getMinecraft(), stack, pos, target, endAnimation.isPresent() ? () -> {
						game.animation(origin == null ? -1 : origin.getId(), List.of(target.getId()),
								endAnimation.get());
					} : () -> {
					}));
	}
}
