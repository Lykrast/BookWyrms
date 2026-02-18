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
			player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.level",
					Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(target.getEnchantingLevel())).withStyle(ChatFormatting.WHITE)
					).withStyle(ChatFormatting.BLUE));
			player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.speed",
					Component.translatable("status.bookwyrms.analyze.speed.format", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(target.getDigestingSpeed())).withStyle(ChatFormatting.WHITE)
					).withStyle(ChatFormatting.BLUE));
			player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.indigest",
					Component.translatable("status.bookwyrms.analyze.indigest.format", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(Math.round(100*target.getIndigestionChance()))).withStyle(ChatFormatting.WHITE)
					).withStyle(ChatFormatting.BLUE));
			if (target.isTreasure()) player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.treasure").withStyle(ChatFormatting.GOLD));
			
			//Digestion
			player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.digested",
					Component.literal(ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(target.getDigestedLevels())).withStyle(ChatFormatting.WHITE)
					).withStyle(ChatFormatting.DARK_GREEN));
			if (target.isDigesting()) {
				if (target.hasMutagen()) {
					Component mutagen = Component.translatable(target.getMutagenString()).withStyle(ChatFormatting.WHITE);
					//mutagen + levels
					if (target.getLevelsToDigest() > 0) {
						player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.digesting",
								Component.translatable("status.bookwyrms.analyze.digesting.both", mutagen, ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(target.getLevelsToDigest())).withStyle(ChatFormatting.WHITE)
								).withStyle(ChatFormatting.DARK_GREEN));
					}
					//just mutagen
					else player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.digesting", mutagen).withStyle(ChatFormatting.DARK_GREEN));
				}
				//just levels
				else if (target.getLevelsToDigest() > 0) {
					player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.digesting",
							Component.translatable("status.bookwyrms.analyze.digesting.levels", ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(target.getLevelsToDigest())).withStyle(ChatFormatting.WHITE)
							).withStyle(ChatFormatting.DARK_GREEN));
				}
				//nothing (likely neutralized mutagen)
				else {
					player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.digesting",
							Component.translatable("status.bookwyrms.analyze.digesting.nothing").withStyle(ChatFormatting.GRAY)
							).withStyle(ChatFormatting.DARK_GREEN));
				}
				//time
				player.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.digesting.time",
						ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(Math.ceil(target.getRemainingDigestTime()/20.0))
						).withStyle(ChatFormatting.GRAY));
			}
			
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.PASS;
	}

}
