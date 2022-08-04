package lykrast.bookwyrms.item;

import lykrast.bookwyrms.registry.ModItems;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class WyrmutagenHelper {
	private static final RegistryObject<?>[] COLORS = {
			ModItems.mutagenGrey, ModItems.mutagenRed, 
			ModItems.mutagenOrange, ModItems.mutagenGreen, 
			ModItems.mutagenBlue, ModItems.mutagenTeal, ModItems.mutagenPurple
			};
	//0 = level up, 1 = level down
	//2 = speed up, 3 = leaving open a speed down and putting Base as a placeholder
	//4 = digestion up, 5 = digestion down
	//6 = stasis
	private static final RegistryObject<?>[] STATS = {
			ModItems.mutagenLvlUp, ModItems.mutagenLvlDn, 
			ModItems.mutagenSpdUp, ModItems.mutagenBase, 
			ModItems.mutagenDgsUp, ModItems.mutagenDgsDown,
			ModItems.mutagenStasis
			};
	public static final int LVL_UP = 0, LVL_DOWN = 1, SPEED_UP = 2, SPEED_DOWN = 3, DIGESTION_UP = 4, DIGESTION_DOWN = 5, STASIS = 6;
	
	// TODO Config?
	public static final int LVL_CHANGE = 8, SPEED_CHANGE = 60;
	public static final double DIGESTION_CHANGE = 0.1;
	
	@SuppressWarnings("unchecked")
	public static String colorName(int color) {
		return ((RegistryObject<Item>)COLORS[color]).get().getDescriptionId();
	}
	
	@SuppressWarnings("unchecked")
	public static String statName(int stat) {
		return ((RegistryObject<Item>)STATS[stat]).get().getDescriptionId();
	}

}
