package lykrast.bookwyrms.registry;

import lykrast.bookwyrms.BookWyrms;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BWSounds {
	public static final DeferredRegister<SoundEvent> REG = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BookWyrms.MODID);
	public static RegistryObject<SoundEvent> wyrmIdle, wyrmHurt, wyrmDeath, wyrmBook, wyrmIndigestion;

	static {
		wyrmIdle = initSound("entity.bookwyrm.idle");
		wyrmHurt = initSound("entity.bookwyrm.hurt");
		wyrmDeath = initSound("entity.bookwyrm.death");
		wyrmBook = initSound("entity.bookwyrm.book");
		wyrmIndigestion = initSound("entity.bookwyrm.indigestion");
	}

	public static RegistryObject<SoundEvent> initSound(String name) {
		return REG.register(name, () -> new SoundEvent(BookWyrms.rl(name)));
	}
}
