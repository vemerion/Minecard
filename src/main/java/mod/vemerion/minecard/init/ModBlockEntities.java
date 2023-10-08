package mod.vemerion.minecard.init;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.blockentity.GameBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
			.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Main.MODID);

	public static final RegistryObject<BlockEntityType<GameBlockEntity>> GAME = BLOCK_ENTITIES.register("game",
			() -> BlockEntityType.Builder.<GameBlockEntity>of(GameBlockEntity::new, ModBlocks.GAME.get()).build(null));
}
