package mod.vemerion.minecard.init;

import mod.vemerion.minecard.advancement.ModCollectCardTrigger;
import mod.vemerion.minecard.advancement.ModFinishGameTrigger;
import mod.vemerion.minecard.advancement.ModGameTrigger;
import net.minecraft.advancements.CriteriaTriggers;

public class ModAdvancements {
	public static ModFinishGameTrigger FINISH_GAME;
	public static ModGameTrigger GAME;
	public static ModCollectCardTrigger COLLECT_CARD;

	public static void register() {
		FINISH_GAME = CriteriaTriggers.register(new ModFinishGameTrigger());
		GAME = CriteriaTriggers.register(new ModGameTrigger());
		COLLECT_CARD = CriteriaTriggers.register(new ModCollectCardTrigger());
	}
}
