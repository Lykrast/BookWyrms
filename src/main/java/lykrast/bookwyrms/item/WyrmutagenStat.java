package lykrast.bookwyrms.item;

import java.util.List;

import lykrast.bookwyrms.config.ConfigValues;
import lykrast.bookwyrms.entity.BookWyrmEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

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

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flags) {
		//the super.super is empty so I think that should be fine?
		//kinda dirty but eeeeh
		switch (stat) {
			case WyrmutagenHelper.LVL_UP:
			case WyrmutagenHelper.LVL_DOWN:
				tooltip.add(Component.translatable(getDescriptionId() + ".desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(ConfigValues.MUTAGEN_LEVEL)).withStyle(ChatFormatting.GRAY));
				break;
			case WyrmutagenHelper.SPEED_UP:
				tooltip.add(Component.translatable(getDescriptionId() + ".desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(Math.round(100*(1-ConfigValues.MUTAGEN_SPEED_MULT)))).withStyle(ChatFormatting.GRAY));
				if (ConfigValues.MUTAGEN_SPEED_PENALTY > 0) tooltip.add(Component.translatable("item.bookwyrms.wyrmutagen_digestion_down.desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(Math.round(100*ConfigValues.MUTAGEN_SPEED_PENALTY))).withStyle(ChatFormatting.GRAY));
				break;
			case WyrmutagenHelper.DIGESTION_UP:
			case WyrmutagenHelper.DIGESTION_DOWN:
				tooltip.add(Component.translatable(getDescriptionId() + ".desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(Math.round(100*ConfigValues.MUTAGEN_INDIGEST))).withStyle(ChatFormatting.GRAY));
				break;
			case WyrmutagenHelper.LVL_UP_SMALL:
				tooltip.add(Component.translatable("item.bookwyrms.wyrmutagen_level_up.desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(1)).withStyle(ChatFormatting.GRAY));
				break;
			case WyrmutagenHelper.LVL_DOWN_SMALL:
				tooltip.add(Component.translatable("item.bookwyrms.wyrmutagen_level_down.desc", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(1)).withStyle(ChatFormatting.GRAY));
				break;
		}
	}

}
