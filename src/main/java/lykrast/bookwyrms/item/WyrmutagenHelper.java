package lykrast.bookwyrms.item;

import lykrast.bookwyrms.registry.BWItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class WyrmutagenHelper {
	private static final RegistryObject<?>[] COLORS = {
			BWItems.mutagenGrey, BWItems.mutagenRed, 
			BWItems.mutagenOrange, BWItems.mutagenGreen, 
			BWItems.mutagenBlue, BWItems.mutagenTeal, BWItems.mutagenPurple
			};
	//0 = level up, 1 = level down
	//2 = speed up, 3 = leaving open a speed down and putting Base as a placeholder
	//4 = digestion up, 5 = digestion down
	//6 = stasis
	private static final RegistryObject<?>[] STATS = {
			BWItems.mutagenLvlUp, BWItems.mutagenLvlDn, 
			BWItems.mutagenSpdUp, BWItems.mutagenBase, 
			BWItems.mutagenDgsUp, BWItems.mutagenDgsDown,
			BWItems.mutagenBase
			};
	public static final int LVL_UP = 0, LVL_DOWN = 1, SPEED_UP = 2, SPEED_DOWN = 3, DIGESTION_UP = 4, DIGESTION_DOWN = 5, LVL_UP_SMALL = 6, LVL_DOWN_SMALL = 7;
	
	@SuppressWarnings("unchecked")
	public static String colorName(int color) {
		return ((RegistryObject<Item>)COLORS[color]).get().getDescriptionId();
	}
	
	@SuppressWarnings("unchecked")
	public static String statName(int stat) {
		return ((RegistryObject<Item>)STATS[stat]).get().getDescriptionId();
	}

}
