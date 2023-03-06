package mod.vemerion.minecard.init;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.block.GameBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MODID);

	public static final RegistryObject<Block> GAME = BLOCKS.register("game", () -> new GameBlock(
			BlockBehaviour.Properties.of(Material.WOOD, MaterialColor.WOOD).strength(2, 3).sound(SoundType.WOOD)));
}
