package lykrast.bookwyrms.registry;

import java.util.function.Supplier;

import lykrast.bookwyrms.BookWyrms;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
	public static RegistryObject<Item> bookWyrmRaw, bookWyrmCooked;
	public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, BookWyrms.MODID);

	static {		
		bookWyrmRaw = initItem("book_wyrm_raw", () -> new Item(defP().food((new FoodProperties.Builder()).nutrition(3).saturationMod(0.3f).meat().build())));
		bookWyrmCooked = initItem("book_wyrm_cooked", () -> new Item(defP().food((new FoodProperties.Builder()).nutrition(8).saturationMod(0.8f).meat().build())));
	}

	public static Item.Properties defP() {
		return new Item.Properties().tab(ItemGroupBookWyrms.INSTANCE);
	}

	public static <I extends Item> RegistryObject<I> initItem(String name, Supplier<I> item) {
		REG.register(name, item);
		return RegistryObject.create(BookWyrms.rl(name), ForgeRegistries.ITEMS);
	}
}
