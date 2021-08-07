package mod.vemerion.minecard;

import net.minecraft.world.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ObjectHolder;

@Mod(Main.MODID)
public class Main {
	public static final String MODID = "minecard";
	
	@ObjectHolder(Main.MODID + ":card")
	public static final Item CARD = null;
}
