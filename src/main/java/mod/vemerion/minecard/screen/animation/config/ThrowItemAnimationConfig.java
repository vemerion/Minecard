package mod.vemerion.minecard.screen.animation.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.ThrowItemAnimation;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec2;
import net.minecraftforge.registries.ForgeRegistries;

public class ThrowItemAnimationConfig extends AnimationConfig {

	public static final Codec<ThrowItemAnimationConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(ThrowItemAnimationConfig::getItem))
			.apply(instance, ThrowItemAnimationConfig::new));

	private final Item item;

	public ThrowItemAnimationConfig(Item item) {
		this.item = item;
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.THROW_ITEM.get();
	}

	public Item getItem() {
		return item;
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		var pos = origin != null ? origin.getPosition() : Vec2.ZERO;

		var stack = item.getDefaultInstance();
		for (var target : targets)
			game.addAnimation(new ThrowItemAnimation(game.getMinecraft(), stack, pos, target, () -> {
			}));
	}
}
