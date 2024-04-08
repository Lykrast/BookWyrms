package lykrast.bookwyrms.item;

import lykrast.bookwyrms.entity.BookWyrmEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AnalyzerItem extends Item {
	public AnalyzerItem(Properties prop) {
		super(prop);
	}

	@SuppressWarnings("resource")
	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
		//Wait what you can syntax like that??
		if (entity instanceof BookWyrmEntity target) {
			if (entity.level().isClientSide) return InteractionResult.SUCCESS;
			
			//Stats
			player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.level", target.getEnchantingLevel()));
			player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.speed", target.getDigestingSpeed()));
			player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.indigest", (int)(100*target.getIndigestionChance())));
			if (target.isTreasure()) player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.treasure"));
			
			//Mutagen
			if (target.hasMutagenColor()) {
				if (target.hasMutagenStat()) {
					player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.wyrmutagen2")
							.append(Component.translatable(WyrmutagenHelper.colorName(target.getMutagenColor())).withStyle(ChatFormatting.WHITE))
							.append(", ").withStyle(ChatFormatting.WHITE)
							.append(Component.translatable(WyrmutagenHelper.statName(target.getMutagenStat())).withStyle(ChatFormatting.WHITE)));
				}
				else {
					player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.wyrmutagen")
							.append(Component.translatable(WyrmutagenHelper.colorName(target.getMutagenColor())).withStyle(ChatFormatting.WHITE)));
				}
			}
			else if (target.hasMutagenStat()) {
				player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.wyrmutagen")
						.append(Component.translatable(WyrmutagenHelper.statName(target.getMutagenStat())).withStyle(ChatFormatting.WHITE)));
			}
			
			//Digestion
			player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.digested", target.getDigestedLevels()));
			if (target.getLevelsToDigest() > 0) player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.todigest", target.getLevelsToDigest()));
			
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

}
