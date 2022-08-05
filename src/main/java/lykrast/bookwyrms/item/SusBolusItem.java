package lykrast.bookwyrms.item;

import java.util.List;

import lykrast.bookwyrms.config.ConfigValues;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class SusBolusItem extends Item {

	public SusBolusItem(Properties prop) {
		super(prop);
	}

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flags) {
		if (ConfigValues.DISABLE_SUS_WARNING) {
			tooltip.add(Component.translatable(getDescriptionId() + ".desc1").withStyle(ChatFormatting.GRAY));
		}
		else {
			tooltip.add(Component.translatable(getDescriptionId() + ".desc1").withStyle(ChatFormatting.RED));
			tooltip.add(Component.translatable(getDescriptionId() + ".desc2").withStyle(ChatFormatting.RED));
		}
		tooltip.add(Component.translatable(getDescriptionId() + ".desc3").withStyle(ChatFormatting.GRAY));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return true;
	}

}
