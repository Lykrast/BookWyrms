package lykrast.bookwyrms.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import lykrast.bookwyrms.BookWyrms;
import lykrast.bookwyrms.entity.BookWyrmEntity;
import lykrast.bookwyrms.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;

public class BWItems {
	public static RegistryObject<Item> analyzer;
	public static RegistryObject<Item> bookWyrmRaw, bookWyrmCooked, chadBolus, chadBolusSus, chadPie;
	public static RegistryObject<Item> bookWyrmSliceRaw, bookWyrmSliceCooked;
	public static RegistryObject<Item> scaleGrey, scaleRed, scaleOrange, scaleGreen, scaleBlue, scaleTeal, scalePurple;
	public static RegistryObject<Item> stewGrey, stewRed, stewOrange, stewGreen, stewBlue, stewTeal, stewPurple;
	public static RegistryObject<Item> mutagenBase, mutagenGrey, mutagenRed, mutagenOrange, mutagenGreen, mutagenBlue, mutagenTeal, mutagenPurple;
	public static RegistryObject<Item> mutagenLvlUp, mutagenLvlDn, mutagenSpdUp, mutagenDgsUp, mutagenDgsDown, mutagenLvlUpS, mutagenLvlDnS;
	public static RegistryObject<Item> spawnEgg;
	public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, BookWyrms.MODID);
	
	private static List<RegistryObject<? extends Item>> orderedItemsCreative = new ArrayList<>();
	
	public static void makeCreativeTab(RegisterEvent event) {
		event.register(Registries.CREATIVE_MODE_TAB, helper -> {
			helper.register(ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(BookWyrms.MODID, "bookwyrms")),
					CreativeModeTab.builder()
					.title(Component.translatable("itemGroup.bookwyrms"))
					.icon(() -> new ItemStack(scaleBlue.get()))
					.displayItems((parameters, output) -> orderedItemsCreative.forEach(i -> output.accept(i.get())))
					.build());
		});
	}
	
	private static final String FARMERS_DELIGHT = "farmersdelight";
	@ObjectHolder(registryName = "minecraft:mob_effect", value = FARMERS_DELIGHT + ":comfort")
	public static MobEffect COMFORT = null;

	static {
		//Put those at the start of the creative tab since they're the most likely things to be wanted
		spawnEgg = initItem("book_wyrm_spawn_egg", () -> new ForgeSpawnEggItem(BWEntities.bookWyrm, 0x808080, 0xFFF9E0, defP()));
		analyzer = initItem("analyzer", () -> new AnalyzerItem(defP().stacksTo(1)));
		
		bookWyrmRaw = initItem("book_wyrm_raw", () -> new Item(defP().food((new FoodProperties.Builder()).nutrition(3).saturationMod(0.3f).meat().build())));
		bookWyrmCooked = initItem("book_wyrm_cooked", () -> new Item(defP().food((new FoodProperties.Builder()).nutrition(8).saturationMod(0.8f).meat().build())));
		if (ModList.get().isLoaded(FARMERS_DELIGHT)) {
			bookWyrmSliceRaw = initItem("book_wyrm_slice_raw", () -> new Item(defP().food((new FoodProperties.Builder()).nutrition(2).saturationMod(0.3f).meat().build())));
			bookWyrmSliceCooked = initItem("book_wyrm_slice_cooked", () -> new Item(defP().food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.8f).meat().build())));
		}
		chadBolus = initItem("chad_bolus", () -> new Item(defP()));
		chadBolusSus = initItem("chad_bolus_suspicious", () -> new SusBolusItem(new Item.Properties()));
		chadPie = initItem("chad_pie", () -> new Item(defP().food((new FoodProperties.Builder()).nutrition(6).saturationMod(0.8f).effect(() -> new MobEffectInstance(MobEffects.HUNGER, 5*20, 0), 0.3f).build())));
		
		scaleGrey = initItem("scale_grey", () -> new Item(defP()));
		scaleRed = initItem("scale_red", () -> new Item(defP()));
		scaleOrange = initItem("scale_orange", () -> new Item(defP()));
		scaleGreen = initItem("scale_green", () -> new Item(defP()));
		scaleBlue = initItem("scale_blue", () -> new Item(defP()));
		scaleTeal = initItem("scale_teal", () -> new Item(defP()));
		scalePurple = initItem("scale_purple", () -> new Item(defP()));

		//Can't feed a null into the effect, so separating it like that
		if (ModList.get().isLoaded(FARMERS_DELIGHT)) {
			stewGrey = initItem("stew_grey", () -> new ContainerFoodItem(stew(new FoodProperties.Builder().nutrition(16).saturationMod(0.8f).effect(() -> new MobEffectInstance(COMFORT, 5*60*20), 1).build())));
		}
		else {
			stewGrey = initItem("stew_grey", () -> new ContainerFoodItem(stew(new FoodProperties.Builder().nutrition(16).saturationMod(0.8f).build())));
		}
		stewRed = initItem("stew_red", () -> new ContainerFoodItem(stew(new FoodProperties.Builder().nutrition(12).saturationMod(0.8f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.DAMAGE_BOOST, 3*60*20), 1).build())));
		stewOrange = initItem("stew_orange", () -> new ContainerFoodItem(stew(new FoodProperties.Builder().nutrition(12).saturationMod(0.8f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3*60*20), 1).build())));
		stewGreen = initItem("stew_green", () -> new ContainerFoodItem(stew(new FoodProperties.Builder().nutrition(12).saturationMod(0.8f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 3*60*20), 1).build())));
		stewBlue = initItem("stew_blue", () -> new ContainerFoodItem(stew(new FoodProperties.Builder().nutrition(12).saturationMod(0.8f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 3*60*20), 1).build())));
		stewTeal = initItem("stew_teal", () -> new ContainerFoodItem(stew(new FoodProperties.Builder().nutrition(12).saturationMod(0.8f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.WATER_BREATHING, 3*60*20), 1).build())));
		stewPurple = initItem("stew_purple", () -> new ContainerFoodItem(stew(new FoodProperties.Builder().nutrition(12).saturationMod(0.8f).alwaysEat().effect(() -> new MobEffectInstance(MobEffects.SLOW_FALLING, 3*30*20), 1).build())));
		
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
		mutagenLvlUpS = initItem("wyrmutagen_level_up_small", () -> new WyrmutagenStat(WyrmutagenHelper.LVL_UP_SMALL, defP()));
		mutagenLvlDnS = initItem("wyrmutagen_level_down_small", () -> new WyrmutagenStat(WyrmutagenHelper.LVL_DOWN_SMALL, defP()));
		mutagenSpdUp = initItem("wyrmutagen_speed_up", () -> new WyrmutagenStat(WyrmutagenHelper.SPEED_UP, defP()));
		mutagenDgsUp = initItem("wyrmutagen_digestion_up", () -> new WyrmutagenStat(WyrmutagenHelper.DIGESTION_UP, defP()));
		mutagenDgsDown = initItem("wyrmutagen_digestion_down", () -> new WyrmutagenStat(WyrmutagenHelper.DIGESTION_DOWN, defP()));
	}

	public static Item.Properties defP() {
		return new Item.Properties();
	}

	public static Item.Properties stew(FoodProperties food) {
		//Same as Farmer's Delight stews
		return defP().craftRemainder(Items.BOWL).stacksTo(16).food(food);
	}

	public static <I extends Item> RegistryObject<I> initItem(String name, Supplier<I> item) {
		REG.register(name, item);
		RegistryObject<I> rego = RegistryObject.create(BookWyrms.rl(name), ForgeRegistries.ITEMS);
		//This one doesn't appear in creative since it's an error message
		if (!name.equals("chad_bolus_suspicious")) orderedItemsCreative.add(rego);
		return rego;
	}
}
