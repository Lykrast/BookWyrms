package lykrast.bookwyrms.registry;

import lykrast.bookwyrms.BookWyrms;
import lykrast.bookwyrms.entity.BookWyrmEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = BookWyrms.MODID)
public class BWEntities {
	public static RegistryObject<EntityType<BookWyrmEntity>> bookWyrm;
	public static final DeferredRegister<EntityType<?>> REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, BookWyrms.MODID);

	static {
		bookWyrm = REG.register("book_wyrm", () -> EntityType.Builder.<BookWyrmEntity>of(BookWyrmEntity::new, MobCategory.CREATURE).sized(0.9f, 0.9f).clientTrackingRange(10).build(""));
	}

	@SubscribeEvent
	public static void registerEntityAttributes(final EntityAttributeCreationEvent event) {
		event.put(bookWyrm.get(), BookWyrmEntity.createAttributes().build());
	}
}
