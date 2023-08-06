package mod.vemerion.minecard.init;

import mod.vemerion.minecard.advancement.ModGameTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public class ModAdvancements {
	public static ModGameTrigger GAME;

	public static void register() {
		GAME = CriteriaTriggers.register(new ModGameTrigger());
	}
}
