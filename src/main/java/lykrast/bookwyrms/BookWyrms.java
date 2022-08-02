package lykrast.bookwyrms;

import lykrast.bookwyrms.registry.ModEntities;
import lykrast.bookwyrms.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BookWyrms.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = BookWyrms.MODID)
public class BookWyrms {
	public static final String MODID = "bookwyrms";
	
	public BookWyrms() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		ModItems.REG.register(bus);
		ModEntities.REG.register(bus);
	}
	
	public static ResourceLocation rl(String name) {
		return new ResourceLocation(MODID, name);
	}
}
