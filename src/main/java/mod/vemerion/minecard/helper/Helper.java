package mod.vemerion.minecard.helper;

import java.util.Calendar;

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

	public static boolean isChristmas() {
		var calendar = Calendar.getInstance();
		var month = calendar.get(Calendar.MONTH);
		var day = calendar.get(Calendar.DAY_OF_MONTH);
		return month == Calendar.DECEMBER && day > 19 && day < 29;
	}

	public static boolean isHalloween() {
		var calendar = Calendar.getInstance();
		var month = calendar.get(Calendar.MONTH);
		var day = calendar.get(Calendar.DAY_OF_MONTH);
		return (month == Calendar.OCTOBER && day > 28) || (month == Calendar.NOVEMBER && day < 4);
	}

	public static boolean isNewYear() {
		var calendar = Calendar.getInstance();
		var month = calendar.get(Calendar.MONTH);
		var day = calendar.get(Calendar.DAY_OF_MONTH);
		return (month == Calendar.DECEMBER && day == 30) || (month == Calendar.JANUARY && day == 1);
	}
}
