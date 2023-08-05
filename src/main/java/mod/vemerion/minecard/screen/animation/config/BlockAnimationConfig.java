package mod.vemerion.minecard.screen.animation.config;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import mod.vemerion.minecard.init.ModAnimationConfigs;
import mod.vemerion.minecard.screen.ClientCard;
import mod.vemerion.minecard.screen.GameScreen;
import mod.vemerion.minecard.screen.animation.BlockAnimation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockAnimationConfig extends AnimationConfig {

	public static final Codec<BlockAnimationConfig> CODEC = RecordCodecBuilder.create(instance -> instance
			.group(ForgeRegistries.BLOCKS.getCodec().fieldOf("block").forGetter(BlockAnimationConfig::getBlock))
			.apply(instance, BlockAnimationConfig::new));

	private final Block block;

	public BlockAnimationConfig(Block block) {
		this.block = block;
	}

	@Override
	protected AnimationConfigType<?> getType() {
		return ModAnimationConfigs.BLOCK.get();
	}

	@Override
	public void invoke(GameScreen game, ClientCard origin, List<ClientCard> targets) {
		if (targets.isEmpty())
			return;

		targets.remove(origin);
		var area = calcArea(targets);
		game.addAnimation(new BlockAnimation(game.getMinecraft(), block, area));
	}

	public Block getBlock() {
		return block;
	}
}
