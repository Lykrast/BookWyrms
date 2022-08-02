package lykrast.bookwyrms.renderer;

import lykrast.bookwyrms.BookWyrms;
import lykrast.bookwyrms.entity.BookWyrmEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class BookWyrmRenderer extends MobRenderer<BookWyrmEntity, BookWyrmModel<BookWyrmEntity>> {
	private static final ResourceLocation[] TEXTURES = {
			BookWyrms.rl("textures/entity/book_wyrm_grey.png"),
			BookWyrms.rl("textures/entity/book_wyrm_red.png"),
			BookWyrms.rl("textures/entity/book_wyrm_orange.png"),
			BookWyrms.rl("textures/entity/book_wyrm_green.png"),
			BookWyrms.rl("textures/entity/book_wyrm_blue.png"),
			BookWyrms.rl("textures/entity/book_wyrm_teal.png"),
			BookWyrms.rl("textures/entity/book_wyrm_purple.png")
	};
	
	public BookWyrmRenderer(Context context) {
		super(context, new BookWyrmModel<>(context.bakeLayer(BookWyrmModel.MODEL)), 0.7f);
	}

	@Override
	public ResourceLocation getTextureLocation(BookWyrmEntity entity) {
		return TEXTURES[entity.getWyrmType()];
	}

}
