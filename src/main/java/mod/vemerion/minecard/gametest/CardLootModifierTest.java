package mod.vemerion.minecard.gametest;

import mod.vemerion.minecard.Main;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

public class CardLootModifierTest {

	@GameTest(template = Main.MODID + ".test")
	public void test(GameTestHelper helper) {
		System.out.println("HELLO HELLO");
	}
}
