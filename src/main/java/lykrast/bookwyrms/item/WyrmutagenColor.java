package lykrast.bookwyrms.item;

import lykrast.bookwyrms.entity.BookWyrmEntity;
import net.minecraft.world.item.ItemStack;

public class WyrmutagenColor extends WyrmutagenItem {
	int color;

	public WyrmutagenColor(int color, Properties prop) {
		super(prop);
		this.color = color;
	}

	@Override
	public void applyMutagen(ItemStack stack, BookWyrmEntity wyrm) {
		wyrm.setMutagenColor(color);
	}

	@Override
	public boolean canApply(ItemStack stack, BookWyrmEntity wyrm) {
		return wyrm.getMutagenColor() != color;
	}

}
