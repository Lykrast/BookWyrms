package lykrast.bookwyrms.item;

import lykrast.bookwyrms.entity.BookWyrmEntity;
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

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player playerIn, LivingEntity entity, InteractionHand hand) {
		//Wait what you can syntax like that??
		if (entity instanceof BookWyrmEntity target) {
			if (entity.level.isClientSide) return net.minecraft.world.InteractionResult.SUCCESS;
			
			//Stats
			playerIn.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.level", target.getEnchantingLevel()));
			playerIn.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.speed", target.getDigestingSpeed()));
			playerIn.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.indigest", (int)(100*target.getIndigestionChance())));
			if (target.isTreasure()) playerIn.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.treasure"));
			
			//Digestion
			playerIn.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.digested", target.getDigestedLevels()));
			if (target.getLevelsToDigest() > 0) playerIn.sendSystemMessage(Component.translatable("status.bookwyrms.analyze.todigest", target.getLevelsToDigest()));
			
			return net.minecraft.world.InteractionResult.SUCCESS;
		}
		return net.minecraft.world.InteractionResult.PASS;
	}

}
