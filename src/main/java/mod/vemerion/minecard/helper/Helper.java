package mod.vemerion.minecard.helper;

import mod.vemerion.minecard.Main;

public class Helper {
	public static String chat(String suffix) {
		return "chat." + Main.MODID + "." + suffix;
	}

	public static String gui(String suffix) {
		return "gui." + Main.MODID + "." + suffix;
	}

	public static String tutorial(int index) {
		return gui("tutorial" + index);
	}
}
