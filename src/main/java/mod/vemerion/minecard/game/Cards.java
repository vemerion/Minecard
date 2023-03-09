package mod.vemerion.minecard.game;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class Cards {
	public static boolean isAllowed(EntityType<?> type) {
		return type.getCategory() != MobCategory.MISC;
	}
}
