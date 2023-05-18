package lykrast.bookwyrms.registry;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ItemGroupBookWyrms extends CreativeModeTab {
	public static final CreativeModeTab INSTANCE = new ItemGroupBookWyrms(CreativeModeTab.getGroupCountSafe(), "bookwyrms");

	public ItemGroupBookWyrms(int index, String label) {
		super(index, label);
	}

	@Override
	public ItemStack makeIcon() {
		return new ItemStack(BWItems.scaleBlue.get());
	}

}
