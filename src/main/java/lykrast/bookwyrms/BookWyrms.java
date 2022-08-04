package lykrast.bookwyrms;

import lykrast.bookwyrms.config.ConfigHolder;
import lykrast.bookwyrms.config.ConfigValues;
import lykrast.bookwyrms.registry.BWEntities;
import lykrast.bookwyrms.registry.BWItems;
import lykrast.bookwyrms.registry.BWSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BookWyrms.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = BookWyrms.MODID)
public class BookWyrms {
	public static final String MODID = "bookwyrms";
	
	public BookWyrms() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		bus.addListener(this::onModConfigEvent);
		BWItems.REG.register(bus);
		BWEntities.REG.register(bus);
		BWSounds.REG.register(bus);
		
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHolder.COMMON_SPEC, "bookwyrms.toml");
	}
	
	public static ResourceLocation rl(String name) {
		return new ResourceLocation(MODID, name);
	}
	
	@SubscribeEvent
    public void onModConfigEvent(final ModConfigEvent event) {
        ModConfig config = event.getConfig();
        // Recalculate the configs when they change
        if (config.getSpec() == ConfigHolder.COMMON_SPEC) ConfigValues.refresh(config);
    }
}
