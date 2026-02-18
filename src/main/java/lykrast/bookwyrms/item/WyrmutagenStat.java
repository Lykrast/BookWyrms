package lykrast.bookwyrms.item;

import lykrast.bookwyrms.entity.BookWyrmEntity;
import net.minecraft.world.item.ItemStack;

public class WyrmutagenStat extends WyrmutagenItem {
	int stat;

	public WyrmutagenStat(int stat, Properties prop) {
		super(prop);
		this.stat = stat;
	}

	@Override
	public void applyMutagen(ItemStack stack, BookWyrmEntity wyrm) {
		wyrm.setMutagenStat(stat);
		wyrm.startDigestingMutagen();
	}

}
