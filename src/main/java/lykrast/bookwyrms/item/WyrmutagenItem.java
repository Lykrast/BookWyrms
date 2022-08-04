package lykrast.bookwyrms.item;

import java.util.List;

import lykrast.bookwyrms.entity.BookWyrmEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public abstract class WyrmutagenItem extends Item {

	public WyrmutagenItem(Properties prop) {
		super(prop);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity entity, InteractionHand hand) {
		//Thanks vanilla for showing me this syntax
		if (entity instanceof BookWyrmEntity wyrm) {
			if (wyrm.isAlive()) {
				if (player.level.isClientSide) return InteractionResult.SUCCESS;
				
				if (canApply(stack, wyrm)) {
					wyrm.level.playSound(player, wyrm, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1, 1);
					applyMutagen(stack, wyrm);
					if (!player.getAbilities().instabuild) stack.shrink(1);

					return InteractionResult.CONSUME;
				}
			}
		}

		return InteractionResult.PASS;
	}
	
	public abstract void applyMutagen(ItemStack stack, BookWyrmEntity wyrm);
	public abstract boolean canApply(ItemStack stack, BookWyrmEntity wyrm);

	@Override
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flags) {
		tooltip.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
	}

}
