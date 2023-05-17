package lykrast.bookwyrms.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ContainerFoodItem extends Item {
	//Modified BowlFoodItem based on Farmer's Delight's ConsumableItem to have proper stacking behavior and stuff
	//https://github.com/vectorwing/FarmersDelight/blob/1.19/src/main/java/vectorwing/farmersdelight/common/item/ConsumableItem.java

	public ContainerFoodItem(Properties properties) {
		super(properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity consumer) {
		ItemStack containerStack = stack.getCraftingRemainingItem();
		
		if (stack.isEdible()) super.finishUsingItem(stack, level, consumer);
		if (stack.isEmpty()) return containerStack;
		else {
			if (consumer instanceof Player player && !((Player) consumer).getAbilities().instabuild) {
				if (!player.getInventory().add(containerStack)) player.drop(containerStack, false);
			}
			return stack;
		}
	}

}
