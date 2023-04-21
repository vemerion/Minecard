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
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.ForgeRegistries;

public class ThrowItemAnimationConfig extends AnimationConfig {

	public static final Codec<ThrowItemAnimationConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(ThrowItemAnimationConfig::getItem),
					ResourceLocation.CODEC.optionalFieldOf("end_animation")
							.forGetter(ThrowItemAnimationConfig::getEndAnimation))
			.apply(instance, ThrowItemAnimationConfig::new));

	private final Item item;
	private final Optional<ResourceLocation> endAnimation;

	public ThrowItemAnimationConfig(Item item, Optional<ResourceLocation> endAnimation) {
		this.item = item;
		this.endAnimation = endAnimation;
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.THROW_ITEM.get();
	}

	public Item getItem() {
		return item;
	}

	public Optional<ResourceLocation> getEndAnimation() {
		return endAnimation;
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		var pos = origin != null
				? origin.getPosition().add(new Vec2(ClientCard.CARD_WIDTH / 2, ClientCard.CARD_HEIGHT / 2))
				: Vec2.ZERO;

		var stack = item.getDefaultInstance();
		for (var target : targets)
			game.addAnimation(
					new ThrowItemAnimation(game.getMinecraft(), stack, pos, target, endAnimation.isPresent() ? () -> {
						game.animation(origin == null ? -1 : origin.getId(), List.of(target.getId()),
								endAnimation.get());
					} : () -> {
					}));
	}
}
