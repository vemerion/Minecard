package mod.vemerion.minecard.datagen;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.init.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class ModBlockStateProvider extends BlockStateProvider {

	public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
		super(output, Main.MODID, exFileHelper);
	}

	@Override
	protected void registerStatesAndModels() {
		game();
	}

	private void game() {
		String name = ForgeRegistries.BLOCKS.getKey(ModBlocks.GAME.get()).getPath();
		ResourceLocation top = modLoc("block/game_top");
		ResourceLocation side = modLoc("block/game_side");
		ResourceLocation leg = modLoc("block/game_leg");
		BlockModelBuilder model = models().withExistingParent(name, mcLoc("block/block")).texture("particle", top)
				.texture("bottom", top).texture("top", top).texture("side", side).texture("leg", leg).renderType("cutout");
		
		// Table
		model.element().from(0, 15 - 3, 0).to(16, 15, 16).face(Direction.DOWN).uvs(0, 0, 16, 16).texture("#bottom")
				.end().face(Direction.UP).uvs(0, 0, 16, 16).texture("#top").end()
				.face(Direction.NORTH).uvs(0, 0, 16, 3).texture("#side").end()
				.face(Direction.SOUTH).uvs(0, 0, 16, 3).texture("#side").end()
				.face(Direction.WEST).uvs(0, 0, 16, 3).texture("#side").end()
				.face(Direction.EAST).uvs(0, 0, 16, 3).texture("#side").end().end();
		
		// Leg
		model.element().from(8 - 2, 3, 8 - 2).to(8 + 2, 15 - 3, 8 + 2).face(Direction.DOWN).uvs(0, 0, 4, 4).texture("#bottom")
		.end().face(Direction.UP).uvs(0, 0, 4, 4).texture("#top").end()
		.face(Direction.NORTH).uvs(0, 0, 4, 16).texture("#leg").end()
		.face(Direction.SOUTH).uvs(0, 0, 4, 16).texture("#leg").end()
		.face(Direction.WEST).uvs(0, 0, 4, 16).texture("#leg").end()
		.face(Direction.EAST).uvs(0, 0, 4, 16).texture("#leg").end().end();
		
		// Top foot
		model.element().from(8 - 4, 1, 8 - 4).to(8 + 4, 3, 8 + 4).face(Direction.DOWN).uvs(0, 0, 8, 8).texture("#top")
		.end().face(Direction.UP).uvs(0, 0, 8, 8).texture("#top").end()
		.face(Direction.NORTH).uvs(0, 0, 8, 3).texture("#top").end()
		.face(Direction.SOUTH).uvs(0, 0, 8, 3).texture("#top").end()
		.face(Direction.WEST).uvs(0, 0, 8, 3).texture("#top").end()
		.face(Direction.EAST).uvs(0, 0, 8, 3).texture("#top").end().end();
		
		// Bottom foot
		model.element().from(8 - 5, 0, 8 - 5).to(8 + 5, 1, 8 + 5).face(Direction.DOWN).uvs(0, 0, 10, 10).texture("#top")
		.end().face(Direction.UP).uvs(0, 0, 10, 10).texture("#top").end()
		.face(Direction.NORTH).uvs(0, 0, 10, 1).texture("#top").end()
		.face(Direction.SOUTH).uvs(0, 0, 10, 1).texture("#top").end()
		.face(Direction.WEST).uvs(0, 0, 10, 1).texture("#top").end()
		.face(Direction.EAST).uvs(0, 0, 10, 1).texture("#top").end().end();
		
		simpleBlock(ModBlocks.GAME.get(), model);
		simpleBlockItem(ModBlocks.GAME.get(), model);
	}
}
