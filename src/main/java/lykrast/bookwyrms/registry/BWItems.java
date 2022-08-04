package lykrast.bookwyrms.registry;

import java.util.function.Supplier;

import lykrast.bookwyrms.BookWyrms;
import lykrast.bookwyrms.entity.BookWyrmEntity;
import lykrast.bookwyrms.item.*;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BWItems {
	public static RegistryObject<Item> analyzer;
	public static RegistryObject<Item> bookWyrmRaw, bookWyrmCooked, chadBolus, chadBolusSus, chadPie;
	public static RegistryObject<Item> scaleGrey, scaleRed, scaleOrange, scaleGreen, scaleBlue, scaleTeal, scalePurple;
	public static RegistryObject<Item> mutagenBase, mutagenGrey, mutagenRed, mutagenOrange, mutagenGreen, mutagenBlue, mutagenTeal, mutagenPurple;
	public static RegistryObject<Item> mutagenLvlUp, mutagenLvlDn, mutagenSpdUp, mutagenDgsUp, mutagenDgsDown, mutagenStasis;
	public static RegistryObject<Item> spawnEgg;
	public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, BookWyrms.MODID);

	static {
		analyzer = initItem("analyzer", () -> new AnalyzerItem(defP().stacksTo(1)));
		
		bookWyrmRaw = initItem("book_wyrm_raw", () -> new Item(defP().food((new FoodProperties.Builder()).nutrition(3).saturationMod(0.3f).meat().build())));
		bookWyrmCooked = initItem("book_wyrm_cooked", () -> new Item(defP().food((new FoodProperties.Builder()).nutrition(8).saturationMod(0.8f).meat().build())));
		chadBolus = initItem("chad_bolus", () -> new Item(defP()));
		//This one doesn't appear in creative since it's an error message
		chadBolusSus = initItem("chad_bolus_suspicious", () -> new SusBolusItem(new Item.Properties()));
		chadPie = initItem("chad_pie", () -> new Item(defP().food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.8f).effect(() -> new MobEffectInstance(MobEffects.HUNGER, 5*20, 0), 0.3f).build())));
		
		scaleGrey = initItem("scale_grey", () -> new Item(defP()));
		scaleRed = initItem("scale_red", () -> new Item(defP()));
		scaleOrange = initItem("scale_orange", () -> new Item(defP()));
		scaleGreen = initItem("scale_green", () -> new Item(defP()));
		scaleBlue = initItem("scale_blue", () -> new Item(defP()));
		scaleTeal = initItem("scale_teal", () -> new Item(defP()));
		scalePurple = initItem("scale_purple", () -> new Item(defP()));
		
		mutagenBase = initItem("wyrmutagen_base", () -> new WyrmutagenBase(defP()));
		mutagenGrey = initItem("wyrmutagen_grey", () -> new WyrmutagenColor(BookWyrmEntity.GREY, defP()));
		mutagenRed = initItem("wyrmutagen_red", () -> new WyrmutagenColor(BookWyrmEntity.RED, defP()));
		mutagenOrange = initItem("wyrmutagen_orange", () -> new WyrmutagenColor(BookWyrmEntity.ORANGE, defP()));
		mutagenGreen = initItem("wyrmutagen_green", () -> new WyrmutagenColor(BookWyrmEntity.GREEN, defP()));
		mutagenBlue = initItem("wyrmutagen_blue", () -> new WyrmutagenColor(BookWyrmEntity.BLUE, defP()));
		mutagenTeal = initItem("wyrmutagen_teal", () -> new WyrmutagenColor(BookWyrmEntity.TEAL, defP()));
		mutagenPurple = initItem("wyrmutagen_purple", () -> new WyrmutagenColor(BookWyrmEntity.PURPLE, defP()));
		mutagenLvlUp = initItem("wyrmutagen_level_up", () -> new WyrmutagenStat(WyrmutagenHelper.LVL_UP, defP()));
		mutagenLvlDn = initItem("wyrmutagen_level_down", () -> new WyrmutagenStat(WyrmutagenHelper.LVL_DOWN, defP()));
		mutagenSpdUp = initItem("wyrmutagen_speed_up", () -> new WyrmutagenStat(WyrmutagenHelper.SPEED_UP, defP()));
		mutagenDgsUp = initItem("wyrmutagen_digestion_up", () -> new WyrmutagenStat(WyrmutagenHelper.DIGESTION_UP, defP()));
		mutagenDgsDown = initItem("wyrmutagen_digestion_down", () -> new WyrmutagenStat(WyrmutagenHelper.DIGESTION_DOWN, defP()));
		mutagenStasis = initItem("wyrmutagen_stasis", () -> new WyrmutagenStat(WyrmutagenHelper.STASIS, defP()));
		
		spawnEgg = initItem("book_wyrm_spawn_egg", () -> new ForgeSpawnEggItem(BWEntities.bookWyrm, 0x3D8DC6, 0xFFF9E0, defP()));
	}

	public static Item.Properties defP() {
		return new Item.Properties().tab(ItemGroupBookWyrms.INSTANCE);
	}

	public static <I extends Item> RegistryObject<I> initItem(String name, Supplier<I> item) {
		REG.register(name, item);
		return RegistryObject.create(BookWyrms.rl(name), ForgeRegistries.ITEMS);
	}
}
