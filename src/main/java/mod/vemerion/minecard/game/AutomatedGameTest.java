package mod.vemerion.minecard.game;

import java.util.ArrayList;
import java.util.List;

import mod.vemerion.minecard.Main;
import mod.vemerion.minecard.blockentity.GameBlockEntity;
import mod.vemerion.minecard.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(modid = Main.MODID, bus = Bus.FORGE)
public class AutomatedGameTest {

	private List<GameBlockEntity> games;

	public AutomatedGameTest(Level level, int count) {
		games = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			var game = new GameBlockEntity(BlockPos.ZERO, ModBlocks.GAME.get().defaultBlockState());
			game.setLevel(level);
			game.addAIPlayer();
			game.addAIPlayer();
			games.add(game);
		}
	}

	public void tick() {
		for (var game : games) {
			game.tick();
		}
	}

//	private static Thread thread;
//
//	@SubscribeEvent
//	public static void test(TickEvent.LevelTickEvent event) {
//		if (event.side == LogicalSide.SERVER && event.phase == Phase.START
//				&& event.level.dimension() == Level.OVERWORLD) {
//			if (thread == null || !thread.isAlive()) {
//				System.out.println("STARTING");
//				thread = new Thread(() -> {
//					var test = new AutomatedGameTest(event.level, 100);
//					for (int i = 0; i < 1000; i++) {
//						test.tick();
//					}
//				});
//				thread.start();
//			}
//		}
//	}
}
