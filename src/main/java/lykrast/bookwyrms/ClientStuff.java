package lykrast.bookwyrms;

import lykrast.bookwyrms.registry.ModEntities;
import lykrast.bookwyrms.renderer.BookWyrmModel;
import lykrast.bookwyrms.renderer.BookWyrmRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = BookWyrms.MODID, value = Dist.CLIENT)
public class ClientStuff {
	
    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
		//Entities
    	event.registerEntityRenderer(ModEntities.bookWyrm.get(), (context) -> new BookWyrmRenderer(context));
    }
    
    @SubscribeEvent
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
    	event.registerLayerDefinition(BookWyrmModel.MODEL, BookWyrmModel::createBodyLayer);
    }

}